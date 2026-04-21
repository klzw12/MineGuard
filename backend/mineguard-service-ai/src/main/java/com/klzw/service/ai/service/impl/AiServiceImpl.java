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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        
        // 计算基于预警记录的驾驶得分
        int warningBasedScore = calculateScoreFromWarnings(trackData);
        
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
            
            // 3. 使用预警得分覆盖AI得分
            if (analysisResult.containsKey("driving_score")) {
                analysisResult.put("driving_score", warningBasedScore);
            }
            
            // 4. 整合结果
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
                
                // 使用预警得分覆盖AI得分
                if (analysisResult.containsKey("driving_score")) {
                    analysisResult.put("driving_score", warningBasedScore);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("analysis", analysisResult);
                result.put("status", "success");
                result.put("message", "驾驶行为分析完成（直接使用原始数据）");
                return result;
            } catch (Exception ex) {
                log.error("分析驾驶行为异常", ex);
                // 返回默认结果，使用预警得分
                Map<String, Object> result = new HashMap<>();
                Map<String, Object> analysis = new HashMap<>();
                analysis.put("driving_score", warningBasedScore);
                analysis.put("recommendations", generateRecommendationsFromWarnings(trackData, warningBasedScore));
                result.put("analysis", analysis);
                result.put("status", "success");
                return result;
            }
        }
    }
    
    /**
     * 根据预警记录计算驾驶得分
     * 基础分100分，根据预警级别扣分：
     * - 高危预警（level=3）：每次扣20分
     * - 中危预警（level=2）：每次扣10分
     * - 低危预警（level=1）：每次扣5分
     * 最低分0分
     */
    private int calculateScoreFromWarnings(Map<String, Object> trackData) {
        int baseScore = 100;
        
        Object warningRecordsObj = trackData.get("warningRecords");
        if (warningRecordsObj == null) {
            return baseScore;
        }
        
        List<Map<String, Object>> warningRecords = (List<Map<String, Object>>) warningRecordsObj;
        if (warningRecords.isEmpty()) {
            return baseScore;
        }
        
        int deduction = 0;
        for (Map<String, Object> record : warningRecords) {
            Object levelObj = record.get("warningLevel");
            if (levelObj != null) {
                int level = ((Number) levelObj).intValue();
                switch (level) {
                    case 3: deduction += 20; break;
                    case 2: deduction += 10; break;
                    case 1: deduction += 5; break;
                }
            }
        }
        
        return Math.max(0, baseScore - deduction);
    }
    
    /**
     * 根据预警记录生成建议
     */
    private String[] generateRecommendationsFromWarnings(Map<String, Object> trackData, int score) {
        List<String> recommendations = new ArrayList<>();
        
        Object warningRecordsObj = trackData.get("warningRecords");
        if (warningRecordsObj != null) {
            List<Map<String, Object>> warningRecords = (List<Map<String, Object>>) warningRecordsObj;
            
            Map<Integer, Integer> levelCounts = new HashMap<>();
            Map<Integer, Set<String>> typeWarnings = new HashMap<>();
            
            for (Map<String, Object> record : warningRecords) {
                Object levelObj = record.get("warningLevel");
                Object typeObj = record.get("warningType");
                Object contentObj = record.get("warningContent");
                
                if (levelObj != null) {
                    int level = ((Number) levelObj).intValue();
                    levelCounts.merge(level, 1, Integer::sum);
                }
                
                if (typeObj != null && contentObj != null) {
                    int type = ((Number) typeObj).intValue();
                    typeWarnings.computeIfAbsent(type, k -> new HashSet<>()).add(contentObj.toString());
                }
            }
            
            // 根据预警级别生成建议
            if (levelCounts.getOrDefault(3, 0) > 0) {
                recommendations.add("存在高危预警，请立即改进驾驶行为");
            }
            if (levelCounts.getOrDefault(2, 0) > 0) {
                recommendations.add("存在中危预警，请注意驾驶安全");
            }
            if (levelCounts.getOrDefault(1, 0) > 0) {
                recommendations.add("存在低危预警，建议优化驾驶习惯");
            }
            
            // 根据预警类型生成具体建议
            if (typeWarnings.containsKey(1)) {
                recommendations.add("注意控制车速，避免超速行驶");
            }
            if (typeWarnings.containsKey(2)) {
                recommendations.add("保持平稳驾驶，避免急加速急减速");
            }
            if (typeWarnings.containsKey(3)) {
                recommendations.add("注意车辆状态，及时检查维护");
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("驾驶行为良好，继续保持");
        }
        
        if (score >= 90) {
            recommendations.add("驾驶评分优秀，请继续保持安全驾驶习惯");
        } else if (score >= 70) {
            recommendations.add("驾驶评分良好，仍有改进空间");
        } else if (score >= 50) {
            recommendations.add("驾驶评分一般，请加强安全意识");
        } else {
            recommendations.add("驾驶评分较低，建议参加安全培训");
        }
        
        return recommendations.toArray(new String[0]);
    }

    /**
     * 生成驾驶行为分析提示词
     * @param trackData 轨迹数据
     * @return 提示词
     */
    private String generateDrivingBehaviorPrompt(Map<String, Object> trackData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下数据分析司机的驾驶行为：\n\n");
        
        // 行程基本信息
        prompt.append("【行程基本信息】\n");
        if (trackData.containsKey("tripId")) {
            prompt.append("行程ID: ").append(trackData.get("tripId")).append("\n");
        }
        if (trackData.containsKey("actualMileage")) {
            prompt.append("实际里程: ").append(trackData.get("actualMileage")).append(" km\n");
        }
        if (trackData.containsKey("actualDuration")) {
            prompt.append("实际时长: ").append(trackData.get("actualDuration")).append(" 分钟\n");
        }
        if (trackData.containsKey("averageSpeed")) {
            prompt.append("平均速度: ").append(trackData.get("averageSpeed")).append(" km/h\n");
        }
        
        // 预警记录信息
        Object warningRecordsObj = trackData.get("warningRecords");
        if (warningRecordsObj != null) {
            List<Map<String, Object>> warningRecords = (List<Map<String, Object>>) warningRecordsObj;
            prompt.append("\n【预警记录】共").append(warningRecords.size()).append("条\n");
            
            Map<Integer, Integer> levelCounts = new HashMap<>();
            for (Map<String, Object> record : warningRecords) {
                Object levelObj = record.get("warningLevel");
                if (levelObj != null) {
                    int level = ((Number) levelObj).intValue();
                    levelCounts.merge(level, 1, Integer::sum);
                }
            }
            
            prompt.append("预警统计: ");
            if (levelCounts.getOrDefault(3, 0) > 0) {
                prompt.append("高危").append(levelCounts.get(3)).append("条 ");
            }
            if (levelCounts.getOrDefault(2, 0) > 0) {
                prompt.append("中危").append(levelCounts.get(2)).append("条 ");
            }
            if (levelCounts.getOrDefault(1, 0) > 0) {
                prompt.append("低危").append(levelCounts.get(1)).append("条 ");
            }
            prompt.append("\n");
            
            // 列出部分预警详情
            prompt.append("预警详情:\n");
            int count = 0;
            for (Map<String, Object> record : warningRecords) {
                if (count >= 5) {
                    prompt.append("... 还有").append(warningRecords.size() - 5).append("条预警\n");
                    break;
                }
                String levelName = "未知";
                Object levelObj = record.get("warningLevel");
                if (levelObj != null) {
                    int level = ((Number) levelObj).intValue();
                    levelName = level == 3 ? "高危" : level == 2 ? "中危" : "低危";
                }
                prompt.append("  - [").append(levelName).append("] ");
                prompt.append(record.getOrDefault("warningContent", "未知预警")).append("\n");
                count++;
            }
        } else {
            prompt.append("\n【预警记录】无预警\n");
        }
        
        // 其他数据
        prompt.append("\n【其他数据】\n");
        for (Map.Entry<String, Object> entry : trackData.entrySet()) {
            if (!entry.getKey().equals("tripId") && 
                !entry.getKey().equals("actualMileage") && 
                !entry.getKey().equals("actualDuration") && 
                !entry.getKey().equals("averageSpeed") && 
                !entry.getKey().equals("warningRecords")) {
                prompt.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        prompt.append("\n请生成以下内容（JSON格式）：\n");
        prompt.append("{\n");
        prompt.append("  \"driving_score\": <根据预警情况计算的分数，0-100>,\n");
        prompt.append("  \"analysis\": \"<驾驶行为综合分析>\",\n");
        prompt.append("  \"speed_analysis\": \"<超速情况分析>\",\n");
        prompt.append("  \"acceleration_analysis\": \"<急加速/急减速情况分析>\",\n");
        prompt.append("  \"recommendations\": [\"<改进建议1>\", \"<改进建议2>\"]\n");
        prompt.append("}\n");
        
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