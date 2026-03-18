package com.klzw.service.user.service.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.klzw.service.user.config.AliyunSmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
    
    // 短信模板定义
    private static final Map<String, String> SMS_TEMPLATES = new HashMap<>();
    
    static {
        // 登录/注册模板
        SMS_TEMPLATES.put("LOGIN_REGISTER", "100001");
        // 修改绑定手机号模板
        SMS_TEMPLATES.put("UPDATE_PHONE", "100002");
        // 重置密码模板
        SMS_TEMPLATES.put("RESET_PASSWORD", "100003");
        // 绑定新手机号模板
        SMS_TEMPLATES.put("BIND_PHONE", "100004");
        // 验证绑定手机号模板
        SMS_TEMPLATES.put("VERIFY_PHONE", "100005");
    }

    private final Map<String, Long> lastSendTimeMap = new ConcurrentHashMap<>();

    @Override
    public boolean sendSmsCode(String phone) {
        return sendLoginRegisterCode(phone);
    }

    @Override
    public boolean sendSmsVerificationCode(String phone, String templateId, String[] params) {
        return sendVerificationCode(phone, templateId != null ? templateId : SMS_TEMPLATES.get("LOGIN_REGISTER"), "通用验证");
    }

    @Override
    public boolean sendLoginRegisterCode(String phone) {
        return sendVerificationCode(phone, SMS_TEMPLATES.get("LOGIN_REGISTER"), "登录/注册");
    }
    
    @Override
    public boolean sendUpdatePhoneCode(String phone) {
        return sendVerificationCode(phone, SMS_TEMPLATES.get("UPDATE_PHONE"), "修改绑定手机号");
    }
    
    @Override
    public boolean sendResetPasswordCode(String phone) {
        return sendVerificationCode(phone, SMS_TEMPLATES.get("RESET_PASSWORD"), "重置密码");
    }
    
    @Override
    public boolean sendBindPhoneCode(String phone) {
        return sendVerificationCode(phone, SMS_TEMPLATES.get("BIND_PHONE"), "绑定新手机号");
    }
    
    @Override
    public boolean sendVerifyPhoneCode(String phone) {
        return sendVerificationCode(phone, SMS_TEMPLATES.get("VERIFY_PHONE"), "验证绑定手机号");
    }

    /**
     * 通用的验证码发送方法
     * @param phone 手机号
     * @param templateCode 模板代码
     * @param operationType 操作类型描述
     * @return 是否发送成功
     */
    private boolean sendVerificationCode(String phone, String templateCode, String operationType) {
        try {
            log.info("[阿里云Dypnsapi] 开始发送{}验证码到手机号: {}", operationType, phone);
            
            // 实现限流机制
            long currentTimeMs = System.currentTimeMillis();
            Long lastSendTimeMs = lastSendTimeMap.get(phone);
            if (lastSendTimeMs != null) {
                long timeElapsedSeconds = (currentTimeMs - lastSendTimeMs) / 1000;
                if (timeElapsedSeconds < SMS_SEND_INTERVAL_SECONDS) {
                    log.warn("[阿里云Dypnsapi] 发送频率过高，手机号: {}, 请等待{}秒后再试", phone, (SMS_SEND_INTERVAL_SECONDS - timeElapsedSeconds));
                    return false;
                }
            }
            
            // 更新最后发送时间
            lastSendTimeMap.put(phone, currentTimeMs);
            
            // 使用阿里云官方SDK发送请求，让阿里云自动生成验证码
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setSignName(aliyunSmsProperties.getSignName())
                    .setTemplateCode(templateCode)
                    .setPhoneNumber(phone)
                    .setTemplateParam("{\"code\":\"##code##\",\"min\":\"" + VERIFICATION_CODE_EXPIRY_MINUTES + "\"}");
            // 使用参数化日志记录，避免直接调用 toString()
            log.debug("[阿里云Dypnsapi] 请求参数 - 签名: {}, 模板代码: {}, 手机号: {}, 模板参数: {}",
                request.getSignName(),
                request.getTemplateCode(),
                request.getPhoneNumber(),
                request.getTemplateParam());

            
            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);

            if (response != null && response.getBody() != null) {
                log.debug("[阿里云Dypnsapi] API响应 - Code: {}, Message: {}, RequestId: {}",
                        response.getBody().getCode(),
                        response.getBody().getMessage(),
                        response.getBody().getRequestId());
            } else {
                log.debug("[阿里云Dypnsapi] API响应为空");
            }
            
            // 检查响应
            if (response != null && response.getBody() != null) {
                if ("OK".equals(response.getBody().getCode())) {
                    log.info("[阿里云Dypnsapi] {}验证码发送成功！手机号: {}", operationType, phone);
                    return true;
                } else {
                    log.error("[阿里云Dypnsapi] {}验证码发送失败，响应码: {}, 手机号: {}", operationType, response.getBody().getCode(), phone);
                    return false;
                }
            } else {
                log.error("[阿里云Dypnsapi] {}验证码发送失败，响应为空，手机号: {}", operationType, phone);
                return false;
            }
        } catch (TeaException error) {
            log.error("[阿里云Dypnsapi] {} TeaException错误: {}, 手机号: {}", operationType, error.getMessage(), phone);
            if (error.getData() != null && error.getData().get("Recommend") != null) {
                log.error("[阿里云Dypnsapi] 诊断地址: {}", error.getData().get("Recommend"));
            }
            return false;
        } catch (Exception e) {
            log.error("[阿里云Dypnsapi] {} 发送短信验证码失败: {}, 手机号: {}", operationType, e.getMessage(), phone, e);
            return false;
        }
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        try {
            log.info("[阿里云Dypnsapi] 开始核验手机号: {}, 验证码: {}", phone, code);
            
            // 构建核验请求
            CheckSmsVerifyCodeRequest checkRequest = new CheckSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setVerifyCode(code);
            
            log.debug("[阿里云Dypnsapi] 核验请求参数: {}", checkRequest.toString());
            
            // 发送核验请求
            RuntimeOptions runtime = new RuntimeOptions();
            com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCodeWithOptions(checkRequest, runtime);

            log.debug("[阿里云Dypnsapi] 核验API响应: {}", response.toString());
            
            // 检查核验结果
            boolean success = response.getBody() != null && "OK".equals(response.getBody().getCode());
            log.info("[阿里云Dypnsapi] 核验结果: {}", success ? "成功" : "失败");
            
            // 如果核验成功，清除发送时间记录，允许用户再次发送验证码
            if (success) {
                lastSendTimeMap.remove(phone);
                log.info("[阿里云Dypnsapi] 核验成功，已清除手机号: {} 的发送时间记录", phone);
            }
            
            return success;
            
        } catch (TeaException error) {
            log.error("[阿里云Dypnsapi] 核验TeaException错误: {}, 手机号: {}", error.getMessage(), phone);
            if (error.getData() != null && error.getData().get("Recommend") != null) {
                log.error("[阿里云Dypnsapi] 诊断地址: {}", error.getData().get("Recommend"));
            }
            log.error("[阿里云Dypnsapi] 核验错误堆栈:", error);
            return false;
        } catch (Exception error) {
            log.error("[阿里云Dypnsapi] 核验Exception错误: {}, 手机号: {}", error.getMessage(), phone);
            log.error("[阿里云Dypnsapi] 核验错误堆栈:", error);
            return false;
        }
    }
}
