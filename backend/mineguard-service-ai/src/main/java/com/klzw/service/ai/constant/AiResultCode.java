package com.klzw.service.ai.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI模块错误码枚举
 * <p>
 * 错误码范围：2600-2699
 */
@Getter
@AllArgsConstructor
public enum AiResultCode {

    AI_MODEL_NOT_FOUND(2600, "AI模型不存在"),
    AI_MODEL_DISABLED(2601, "AI模型已禁用"),
    AI_ANALYSIS_FAILED(2610, "AI分析失败"),
    AI_PREDICTION_FAILED(2620, "AI预测失败"),
    AI_ANOMALY_DETECTION_FAILED(2630, "异常检测失败"),
    AI_PROVIDER_NOT_AVAILABLE(2640, "AI服务提供商不可用"),
    AI_API_KEY_INVALID(2641, "AI API密钥无效"),
    AI_REQUEST_TIMEOUT(2642, "AI请求超时");

    private final int code;
    private final String message;
}
