package com.klzw.service.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "短信验证码响应")
public class SmsCodeVO {

    @Schema(description = "短信验证码ID")
    private String smsId;

    @Schema(description = "是否发送成功")
    private Boolean success;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "剩余可发送时间（秒），如果发送频率超限则返回")
    private Long remainingSeconds;
}
