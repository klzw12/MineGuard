package com.klzw.service.user.service.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.klzw.service.user.config.AliyunSmsProperties;
import com.klzw.service.user.enums.SmsScene;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mineguard.sms.aliyun.enabled", havingValue = "true")
public class AliyunSmsServiceImpl implements SmsService {

    private final Client client;
    private final AliyunSmsProperties aliyunSmsProperties;

    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;
    private static final int SMS_SEND_INTERVAL_SECONDS = 60;

    private final Map<String, Long> lastSendTimeMap = new ConcurrentHashMap<>();

    @Override
    public boolean sendSmsCode(String phone) {
        return sendSmsCode(phone, SmsScene.REGISTER);
    }

    @Override
    public boolean sendSmsCode(String phone, SmsScene scene) {
        return sendVerificationCode(phone, scene);
    }

    private boolean sendVerificationCode(String phone, SmsScene scene) {
        try {
            log.info("[阿里云短信] 开始发送{}验证码到手机号: {}", scene.getDescription(), phone);
            
            long currentTimeMs = System.currentTimeMillis();
            Long lastSendTimeMs = lastSendTimeMap.get(phone);
            if (lastSendTimeMs != null) {
                long timeElapsedSeconds = (currentTimeMs - lastSendTimeMs) / 1000;
                if (timeElapsedSeconds < SMS_SEND_INTERVAL_SECONDS) {
                    log.warn("[阿里云短信] 发送频率过高，手机号: {}, 请等待{}秒后再试", phone, (SMS_SEND_INTERVAL_SECONDS - timeElapsedSeconds));
                    return false;
                }
            }
            
            lastSendTimeMap.put(phone, currentTimeMs);
            
            String templateCode = aliyunSmsProperties.getTemplateCode(scene.getCode());
            
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setSignName(aliyunSmsProperties.getSignName())
                    .setTemplateCode(templateCode)
                    .setPhoneNumber(phone)
                    .setTemplateParam("{\"code\":\"##code##\",\"min\":\"" + VERIFICATION_CODE_EXPIRY_MINUTES + "\"}");
            
            log.debug("[阿里云短信] 请求参数 - 签名: {}, 模板代码: {}, 手机号: {}, 场景: {}",
                request.getSignName(),
                request.getTemplateCode(),
                request.getPhoneNumber(),
                scene.getDescription());

            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);

            if (response != null && response.getBody() != null) {
                log.debug("[阿里云短信] API响应 - Code: {}, Message: {}, RequestId: {}",
                        response.getBody().getCode(),
                        response.getBody().getMessage(),
                        response.getBody().getRequestId());
            }
            
            if (response != null && response.getBody() != null) {
                if ("OK".equals(response.getBody().getCode())) {
                    log.info("[阿里云短信] {}验证码发送成功！手机号: {}", scene.getDescription(), phone);
                    return true;
                } else {
                    log.error("[阿里云短信] {}验证码发送失败，响应码: {}, 手机号: {}", scene.getDescription(), response.getBody().getCode(), phone);
                    return false;
                }
            } else {
                log.error("[阿里云短信] {}验证码发送失败，响应为空，手机号: {}", scene.getDescription(), phone);
                return false;
            }
        } catch (TeaException error) {
            log.error("[阿里云短信] {} TeaException错误: {}, 手机号: {}", scene.getDescription(), error.getMessage(), phone);
            if (error.getData() != null && error.getData().get("Recommend") != null) {
                log.error("[阿里云短信] 诊断地址: {}", error.getData().get("Recommend"));
            }
            return false;
        } catch (Exception e) {
            log.error("[阿里云短信] {} 发送短信验证码失败: {}, 手机号: {}", scene.getDescription(), e.getMessage(), phone, e);
            return false;
        }
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        try {
            log.info("[阿里云短信] 开始核验手机号: {}", phone);
            
            CheckSmsVerifyCodeRequest checkRequest = new CheckSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setVerifyCode(code);
            
            RuntimeOptions runtime = new RuntimeOptions();
            com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCodeWithOptions(checkRequest, runtime);

            boolean success = response.getBody() != null && "OK".equals(response.getBody().getCode());
            log.info("[阿里云短信] 核验结果: {}", success ? "成功" : "失败");
            
            if (success) {
                lastSendTimeMap.remove(phone);
                log.info("[阿里云短信] 核验成功，已清除手机号: {} 的发送时间记录", phone);
            }
            
            return success;
            
        } catch (TeaException error) {
            log.error("[阿里云短信] 核验TeaException错误: {}, 手机号: {}", error.getMessage(), phone);
            if (error.getData() != null && error.getData().get("Recommend") != null) {
                log.error("[阿里云短信] 诊断地址: {}", error.getData().get("Recommend"));
            }
            return false;
        } catch (Exception error) {
            log.error("[阿里云短信] 核验Exception错误: {}, 手机号: {}", error.getMessage(), phone);
            return false;
        }
    }
}
