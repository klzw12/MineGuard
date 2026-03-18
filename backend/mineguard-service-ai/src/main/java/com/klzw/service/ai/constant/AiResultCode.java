package com.klzw.service.ai.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiResultCode {

    AI_MODEL_NOT_FOUND(2400, "AI模型不存在"),
    AI_MODEL_DISABLED(2401, "AI模型已禁用"),
    AI_ANALYSIS_FAILED(2410, "AI分析失败"),
    AI_PREDICTION_FAILED(2420, "AI预测失败"),
    AI_ANOMALY_DETECTION_FAILED(2430, "异常检测失败"),
    AI_PROVIDER_NOT_AVAILABLE(2440, "AI服务提供商不可用"),
    AI_API_KEY_INVALID(2441, "AI API密钥无效"),
    AI_REQUEST_TIMEOUT(2442, "AI请求超时");

    private final int code;
    private final String message;
}
