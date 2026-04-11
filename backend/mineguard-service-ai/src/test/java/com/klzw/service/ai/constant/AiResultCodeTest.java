package com.klzw.service.ai.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiResultCodeTest {

    @Test
    void testAllResultCodes_HaveCorrectCodeAndMessage() {
        assertEquals(2600, AiResultCode.AI_MODEL_NOT_FOUND.getCode());
        assertEquals("AI模型不存在", AiResultCode.AI_MODEL_NOT_FOUND.getMessage());

        assertEquals(2601, AiResultCode.AI_MODEL_DISABLED.getCode());
        assertEquals("AI模型已禁用", AiResultCode.AI_MODEL_DISABLED.getMessage());

        assertEquals(2610, AiResultCode.AI_ANALYSIS_FAILED.getCode());
        assertEquals("AI分析失败", AiResultCode.AI_ANALYSIS_FAILED.getMessage());

        assertEquals(2620, AiResultCode.AI_PREDICTION_FAILED.getCode());
        assertEquals("AI预测失败", AiResultCode.AI_PREDICTION_FAILED.getMessage());

        assertEquals(2630, AiResultCode.AI_ANOMALY_DETECTION_FAILED.getCode());
        assertEquals("异常检测失败", AiResultCode.AI_ANOMALY_DETECTION_FAILED.getMessage());

        assertEquals(2640, AiResultCode.AI_PROVIDER_NOT_AVAILABLE.getCode());
        assertEquals("AI服务提供商不可用", AiResultCode.AI_PROVIDER_NOT_AVAILABLE.getMessage());

        assertEquals(2641, AiResultCode.AI_API_KEY_INVALID.getCode());
        assertEquals("AI API密钥无效", AiResultCode.AI_API_KEY_INVALID.getMessage());

        assertEquals(2642, AiResultCode.AI_REQUEST_TIMEOUT.getCode());
        assertEquals("AI请求超时", AiResultCode.AI_REQUEST_TIMEOUT.getMessage());
    }

    @Test
    void testResultCodes_InRange() {
        for (AiResultCode code : AiResultCode.values()) {
            assertTrue(code.getCode() >= 2600 && code.getCode() <= 2699,
                    "错误码 " + code.name() + " 不在2600-2699范围内");
        }
    }

    @Test
    void testResultCodes_NoDuplicateCodes() {
        java.util.Set<Integer> codes = new java.util.HashSet<>();
        for (AiResultCode code : AiResultCode.values()) {
            assertTrue(codes.add(code.getCode()),
                    "发现重复错误码: " + code.getCode());
        }
    }
}
