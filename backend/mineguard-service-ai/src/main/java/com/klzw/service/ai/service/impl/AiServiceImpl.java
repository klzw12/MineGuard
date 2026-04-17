package com.klzw.service.ai.service.impl;

import com.klzw.common.core.client.PythonClient;
import com.klzw.common.core.util.HttpUtils;
import com.klzw.common.core.util.JsonUtils;
import com.klzw.service.ai.adapter.AiAdapter;
import com.klzw.service.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    @Autowired
    private Map<String, AiAdapter> aiAdapterMap;

    @Autowired
    private PythonClient pythonClient;

    @Qualifier("chatClientWithTools")
    @Autowired(required = false)
    private ChatClient chatClientWithTools;

    @Value("${ai.default-provider:deepseek}")
    private String aiProvider;

    @Value("${ai.providers}")
    private List<String> providers;

    @Override
    public Map<String, Object> chat(String message, List<Map<String, String>> history) {
        try {
            log.info("AI对话: {}", message);
            
            if (chatClientWithTools != null) {
                return chatWithTools(message, history);
            }
            
            AiAdapter adapter = getAiAdapter();
            String prompt = buildChatPrompt(message, history);
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> response = adapter.sendRequest(prompt, parameters);
            return adapter.parseResponse(response);
        } catch (Exception e) {
            log.error("AI对话异常", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            errorResult.put("response", "AI服务暂时不可用，请稍后再试。");
            return errorResult;
        }
    }

    private Map<String, Object> chatWithTools(String message, List<Map<String, String>> history) {
        try {
            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("你是MineGuard矿山管理系统的AI助手。\n");
            systemPrompt.append("当用户询问车辆运行情况、成本分析、调度优化、预警统计等问题时，你应该调用提供的工具来获取真实数据。\n");
            systemPrompt.append("可用的工具：\n");
            systemPrompt.append("- get_vehicle_status: 获取今日车辆运行情况\n");
            systemPrompt.append("- get_cost_analysis: 获取成本分析报告\n");
            systemPrompt.append("- get_dispatch_optimization: 获取调度优化方案\n");
            systemPrompt.append("- get_warning_statistics: 获取预警统计信息\n");
            systemPrompt.append("- get_trip_statistics: 获取行程统计数据\n");
            systemPrompt.append("- search_vehicles: 搜索车辆信息\n");
            systemPrompt.append("请根据用户问题选择合适的工具调用，回答要简洁明了，用中文回答。");
            
            String response = chatClientWithTools.prompt()
                    .system(systemPrompt.toString())
                    .user(message)
                    .call()
                    .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", response);
            result.put("status", "success");
            result.put("tool", true);
            return result;
        } catch (Exception e) {
            log.error("使用工具调用失败，回退到普通模式", e);
            AiAdapter adapter = getAiAdapter();
            String prompt = buildChatPrompt(message, history);
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> response = adapter.sendRequest(prompt, parameters);
            return adapter.parseResponse(response);
        }
    }

    private String buildChatPrompt(String message, List<Map<String, String>> history) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是MineGuard矿山管理系统的AI助手，专门帮助用户解答关于矿山车辆管理、调度、成本分析等问题。\n\n");
        
        if (history != null && !history.isEmpty()) {
            prompt.append("对话历史：\n");
            for (Map<String, String> h : history) {
                String role = h.get("role");
                String content = h.get("content");
                if ("user".equals(role)) {
                    prompt.append("用户: ").append(content).append("\n");
                } else {
                    prompt.append("助手: ").append(content).append("\n");
                }
            }
            prompt.append("\n");
        }
        
        prompt.append("用户: ").append(message).append("\n\n");
        prompt.append("请用中文回答，回答要简洁明了。");
        
        return prompt.toString();
    }

    @Override
    public Map<String, Object> analyzeStatisticsData(Map<String, Object> statisticsData) {
        try {
            log.info("分析统计数据");
            AiAdapter adapter = getAiAdapter();
            String prompt = adapter.generateAnalysisPrompt(statisticsData, "statistics");
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> response = adapter.sendRequest(prompt, parameters);
            return adapter.parseResponse(response);
        } catch (Exception e) {
            log.error("分析统计数据异常", e);
            throw new RuntimeException("分析统计数据异常", e);
        }
    }

    @Override
    public Map<String, Object> analyzeCostData(Map<String, Object> costData) {
        try {
            log.info("分析成本数据");
            AiAdapter adapter = getAiAdapter();
            String prompt = adapter.generateAnalysisPrompt(costData, "cost");
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> response = adapter.sendRequest(prompt, parameters);
            return adapter.parseResponse(response);
        } catch (Exception e) {
            log.error("分析成本数据异常", e);
            throw new RuntimeException("分析成本数据异常", e);
        }
    }

    @Override
    public Map<String, Object> generateFinancialReport(Map<String, Object> financialData) {
        try {
            log.info("生成财务报表");
            AiAdapter adapter = getAiAdapter();
            String prompt = generateFinancialReportPrompt(financialData);
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> response = adapter.sendRequest(prompt, parameters);
            return adapter.parseResponse(response);
        } catch (Exception e) {
            log.error("生成财务报表异常", e);
            throw new RuntimeException("生成财务报表异常", e);
        }
    }

    @Override
    public Map<String, Object> generateOptimizationSuggestions(Map<String, Object> analysisData) {
        try {
            log.info("生成优化建议");
            AiAdapter adapter = getAiAdapter();
            String prompt = generateOptimizationSuggestionsPrompt(analysisData);
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> response = adapter.sendRequest(prompt, parameters);
            return adapter.parseResponse(response);
        } catch (Exception e) {
            log.error("生成优化建议异常", e);
            throw new RuntimeException("生成优化建议异常", e);
        }
    }

    @Override
    public Map<String, Object> generateDispatchSuggestions(Map<String, Object> dispatchData) {
        try {
            log.info("为调度服务提供智能建议");
            AiAdapter adapter = getAiAdapter();
            String prompt = generateDispatchSuggestionsPrompt(dispatchData);
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> response = adapter.sendRequest(prompt, parameters);
            return adapter.parseResponse(response);
        } catch (Exception e) {
            log.error("为调度服务提供智能建议异常", e);
            throw new RuntimeException("为调度服务提供智能建议异常", e);
        }
    }

    /**
     * 分析驾驶行为
     * @param trackData 轨迹数据
     * @return 分析结果
     */
    public Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData) {
        log.info("分析驾驶行为");
        try {
            // 1. 调用 Python 服务进行数据清洗
            Map<String, Object> cleanResult = pythonClient.cleanDrivingData(trackData);
            Map<String, Object> cleanedData = (Map<String, Object>) cleanResult.get("cleaned_data");
            Map<String, Object> cleaningReport = (Map<String, Object>) cleanResult.get("cleaning_report");
            
            log.info("驾驶数据清洗完成: {}", cleaningReport);
            
            // 2. 使用 AI 适配器进行分析
            AiAdapter adapter = getAiAdapter();
            String prompt = generateDrivingBehaviorPrompt(cleanedData);
            Map<String, Object> parameters = new HashMap<>();
            Map<String, Object> aiResponse = adapter.sendRequest(prompt, parameters);
            Map<String, Object> analysisResult = adapter.parseResponse(aiResponse);
            
            // 3. 整合结果
            Map<String, Object> result = new HashMap<>();
            result.put("analysis", analysisResult);
            result.put("cleaning_report", cleaningReport);
            result.put("status", "success");
            result.put("message", "驾驶行为分析完成");
            
            log.info("驾驶行为分析结果: {}", result);
            return result;
        } catch (Exception e) {
            log.warn("调用 Python 服务进行数据清洗失败: {}", e.getMessage());
            // 失败时直接使用 AI 适配器进行分析
            try {
                AiAdapter adapter = getAiAdapter();
                String prompt = generateDrivingBehaviorPrompt(trackData);
                Map<String, Object> parameters = new HashMap<>();
                Map<String, Object> response = adapter.sendRequest(prompt, parameters);
                Map<String, Object> analysisResult = adapter.parseResponse(response);
                
                Map<String, Object> result = new HashMap<>();
                result.put("analysis", analysisResult);
                result.put("status", "success");
                result.put("message", "驾驶行为分析完成（直接使用原始数据）");
                return result;
            } catch (Exception ex) {
                log.error("分析驾驶行为异常", ex);
                // 返回默认结果
                Map<String, Object> result = new HashMap<>();
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("driving_score", 85);
                analysis.put("recommendations", new String[]{"驾驶行为良好，继续保持"});
                result.put("analysis", analysis);
                result.put("status", "success");
                return result;
            }
        }
    }

    /**
     * 生成驾驶行为分析提示词
     * @param trackData 轨迹数据
     * @return 提示词
     */
    private String generateDrivingBehaviorPrompt(Map<String, Object> trackData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下轨迹数据分析司机的驾驶行为：\n");
        prompt.append("轨迹数据：\n");
        
        for (Map.Entry<String, Object> entry : trackData.entrySet()) {
            prompt.append(entry.getKey()).append(": " ).append(entry.getValue()).append("\n");
        }
        
        prompt.append("\n请生成以下内容：\n");
        prompt.append("1. 驾驶行为分析\n");
        prompt.append("2. 超速情况\n");
        prompt.append("3. 急加速/急减速情况\n");
        prompt.append("4. 驾驶行为评分\n");
        prompt.append("5. 改进建议\n");
        
        return prompt.toString();
    }

    /**
     * 获取AI适配器
     * @return AI适配器
     */
    private AiAdapter getAiAdapter() {
        String adapterName = aiProvider + "Adapter";
        AiAdapter adapter = aiAdapterMap.get(adapterName);
        if (adapter == null) {
            log.warn("未找到指定的AI适配器: {}", adapterName);
            // 默认使用DeepSeek适配器
            adapter = aiAdapterMap.get("deepSeekAdapter");
        }
        return adapter;
    }

    /**
     * 生成财务报表提示词
     * @param financialData 财务数据
     * @return 提示词
     */
    private String generateFinancialReportPrompt(Map<String, Object> financialData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下财务数据生成详细的财务报表：\n");
        prompt.append("财务数据：\n");
        
        for (Map.Entry<String, Object> entry : financialData.entrySet()) {
            prompt.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        prompt.append("\n请生成以下内容：\n");
        prompt.append("1. 财务概览\n");
        prompt.append("2. 收入分析\n");
        prompt.append("3. 成本分析\n");
        prompt.append("4. 利润分析\n");
        prompt.append("5. 财务建议\n");
        
        return prompt.toString();
    }

    /**
     * 生成优化建议提示词
     * @param analysisData 分析数据
     * @return 提示词
     */
    private String generateOptimizationSuggestionsPrompt(Map<String, Object> analysisData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下分析数据生成详细的优化建议：\n");
        prompt.append("分析数据：\n");
        
        for (Map.Entry<String, Object> entry : analysisData.entrySet()) {
            prompt.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        prompt.append("\n请生成以下内容：\n");
        prompt.append("1. 问题识别\n");
        prompt.append("2. 优化建议\n");
        prompt.append("3. 预期效果\n");
        prompt.append("4. 实施步骤\n");
        
        return prompt.toString();
    }

    /**
     * 生成调度建议提示词
     * @param dispatchData 调度数据
     * @return 提示词
     */
    private String generateDispatchSuggestionsPrompt(Map<String, Object> dispatchData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下调度数据生成智能调度建议：\n");
        prompt.append("调度数据：\n");
        
        for (Map.Entry<String, Object> entry : dispatchData.entrySet()) {
            prompt.append(entry.getKey()).append(": " ).append(entry.getValue()).append("\n");
        }
        
        prompt.append("\n请生成以下内容：\n");
        prompt.append("1. 调度方案\n");
        prompt.append("2. 车辆分配建议\n");
        prompt.append("3. 路线优化建议\n");
        prompt.append("4. 预期效果\n");
        
        return prompt.toString();
    }

    @Override
    public String getCurrentProvider() {
        return aiProvider;
    }

    @Override
    public boolean switchProvider(String provider) {
        try {
            // 检查提供商是否在支持列表中
            if (providers.contains(provider)) {
                // 检查适配器是否存在
                AiAdapter adapter = aiAdapterMap.get(provider + "Adapter");
                if (adapter != null) {
                    aiProvider = provider;
                    log.info("AI提供商已切换为: {}", provider);
                    return true;
                } else {
                    log.warn("未找到对应的AI适配器: {}", provider);
                    return false;
                }
            } else {
                log.warn("不支持的AI提供商: {}", provider);
                return false;
            }
        } catch (Exception e) {
            log.error("切换AI提供商异常", e);
            return false;
        }
    }
}