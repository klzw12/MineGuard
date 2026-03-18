package com.klzw.service.ai.adapter;

import java.util.Map;

public interface AiAdapter {

    /**
     * 发送请求到AI API
     * @param prompt 提示词
     * @param parameters 参数
     * @return AI响应
     */
    Map<String, Object> sendRequest(String prompt, Map<String, Object> parameters);

    /**
     * 生成分析提示词
     * @param data 分析数据
     * @param analysisType 分析类型
     * @return 提示词
     */
    String generateAnalysisPrompt(Map<String, Object> data, String analysisType);

    /**
     * 解析AI响应
     * @param response AI响应
     * @return 解析结果
     */
    Map<String, Object> parseResponse(Map<String, Object> response);
}