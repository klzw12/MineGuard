package com.klzw.common.file.handler;

import com.klzw.common.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板OCR解析器
 * 用于解析通用文字识别API返回的结果
 */
@Slf4j
@Component
public class TemplateOcrParser implements OcrParser {

    /**
     * 安全解析JSON字符串，提取words_result数组
     * @param ocrResult OCR识别结果
     * @return words_result数组，解析失败返回空列表
     */
    private List<Map<String, Object>> safeParseJson(String ocrResult) {
        if (ocrResult == null || ocrResult.trim().isEmpty()) {
            log.warn("OCR结果为空");
            return List.of();
        }
        
        try {
            log.debug("开始解析OCR结果，长度: {}", ocrResult.length());
            log.debug("OCR结果前200字符: {}", ocrResult.substring(0, Math.min(200, ocrResult.length())));
            
            Map<String, Object> jsonObject = JsonUtils.fromJson(ocrResult, new TypeReference<Map<String, Object>>() {});
            if (jsonObject == null) {
                log.warn("JSON解析结果为null");
                return List.of();
            }
            
            log.debug("JSON解析成功，keys: {}", jsonObject.keySet());
            
            if (jsonObject.containsKey("words_result")) {
                Object wordsResult = jsonObject.get("words_result");
                log.debug("words_result类型: {}", wordsResult != null ? wordsResult.getClass().getName() : "null");
                
                if (wordsResult instanceof List) {
                    List<?> list = (List<?>) wordsResult;
                    log.debug("words_result是List，大小: {}", list.size());
                    return (List<Map<String, Object>>) wordsResult;
                } else {
                    String wordsResultJson = JsonUtils.toJson(wordsResult);
                    return JsonUtils.fromJson(wordsResultJson, new TypeReference<List<Map<String, Object>>>() {});
                }
            } else {
                log.warn("JSON中不包含words_result字段，实际字段: {}", jsonObject.keySet());
            }
        } catch (Exception e) {
            log.error("解析JSON失败: {}, OCR结果前500字符: {}", e.getMessage(), 
                    ocrResult.substring(0, Math.min(500, ocrResult.length())), e);
        }
        return List.of();
    }

    /**
     * 通用解析方法，根据关键字映射解析证件信息，支持不同格式的冒号
     * @param ocrResult OCR识别结果
     * @param keywordMap 关键字到结果键的映射
     * @return 解析结果
     */
    private Map<String, String> parseWithKeywordMap(String ocrResult, Map<String, String> keywordMap) {
        Map<String, String> result = new HashMap<>();
        List<Map<String, Object>> wordsResult = safeParseJson(ocrResult);
        
        for (Map<String, Object> wordItem : wordsResult) {
            safeProcessWordItem(wordItem, keywordMap, result);
        }
        
        for (Map.Entry<String, String> entry : result.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (isDateField(key) && value != null) {
                result.put(key, normalizeDate(value));
            }
        }
        return result;
    }
    
    private boolean isDateField(String key) {
        return key != null && (key.toLowerCase().contains("date") || 
               key.equals("registerDate") || key.equals("issueDate") ||
               key.equals("validUntil") || key.equals("birth") ||
               key.equals("firstIssueDate") || key.equals("validPeriod"));
    }
    
    private String normalizeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return dateStr;
        }
        
        String normalized = dateStr
            .replace("年", "-")
            .replace("月", "-")
            .replace("日", "")
            .replace("/", "-");
        
        if (normalized.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            String[] parts = normalized.split("-");
            if (parts.length == 3) {
                String year = parts[0];
                String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
                String day = parts[2].length() == 1 ? "0" + parts[2] : parts[2];
                return year + "-" + month + "-" + day;
            }
        }
        
        return normalized;
    }
    
    /**
     * 安全处理单个单词条目
     */
    private void safeProcessWordItem(Map<String, Object> wordItem, Map<String, String> keywordMap, Map<String, String> result) {
        try {
            String words = (String) wordItem.get("words");
            if (words == null) {
                return;
            }
            
            for (Map.Entry<String, String> entry : keywordMap.entrySet()) {
                String keyword = entry.getKey();
                String key = entry.getValue();
                
                // 尝试匹配不同格式
                String value = extractValue(words, keyword);
                if (value != null) {
                    result.put(key, value);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("解析条目失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 从文本中提取值，支持不同格式的冒号
     * @param text 原始文本
     * @param keyword 关键字
     * @return 提取的值，未找到返回null
     */
    private String extractValue(String text, String keyword) {
        // 移除冒号后的关键字
        String keywordWithoutColon = keyword.replaceAll("[：:]", "");
        
        // 尝试匹配原始关键字（包含冒号）
        if (text.contains(keyword)) {
            String value = text.replace(keyword, "").trim();
            // 去除值开头的冒号
            return removeLeadingColon(value);
        }
        
        // 尝试匹配中文冒号
        String keywordWithChineseColon = keywordWithoutColon + "：";
        if (text.contains(keywordWithChineseColon)) {
            String value = text.replace(keywordWithChineseColon, "").trim();
            return removeLeadingColon(value);
        }
        
        // 尝试匹配英文冒号
        String keywordWithEnglishColon = keywordWithoutColon + ":";
        if (text.contains(keywordWithEnglishColon)) {
            String value = text.replace(keywordWithEnglishColon, "").trim();
            return removeLeadingColon(value);
        }
        
        // 尝试匹配无冒号的情况（关键字后面直接跟值）
        if (text.startsWith(keywordWithoutColon)) {
            String value = text.substring(keywordWithoutColon.length()).trim();
            // 去除值开头的冒号
            return removeLeadingColon(value);
        }
        
        return null;
    }
    
    /**
     * 去除字符串开头的冒号（中文或英文）
     */
    private String removeLeadingColon(String value) {
        if (value.startsWith("：") || value.startsWith(":")) {
            return value.substring(1).trim();
        }
        return value;
    }

    @Override
    public Map<String, String> parseIdCard(String ocrResult) {
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("姓名：", "name");
        keywordMap.put("性别：", "gender");
        keywordMap.put("民族：", "nation");
        keywordMap.put("出生日期：", "birth");
        keywordMap.put("住址：", "address");
        keywordMap.put("公民身份号码", "idNumber");
        return parseWithKeywordMap(ocrResult, keywordMap);
    }

    @Override
    public Map<String, String> parseDrivingLicense(String ocrResult) {
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("姓名：", "name");
        keywordMap.put("性别：", "gender");
        keywordMap.put("国籍：", "nationality");
        keywordMap.put("住址：", "address");
        keywordMap.put("出生日期：", "birth");
        keywordMap.put("初次领证日期：", "firstIssueDate");
        keywordMap.put("准驾车型：", "准驾车型");
        keywordMap.put("有效期限：", "validPeriod");
        keywordMap.put("证号：", "licenseNumber");
        return parseWithKeywordMap(ocrResult, keywordMap);
    }

    @Override
    public Map<String, String> parseRepairCert(String ocrResult) {
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("证书编号：", "certificateNumber");
        keywordMap.put("姓名：", "name");
        keywordMap.put("身份证号：", "idNumber");
        keywordMap.put("资格等级：", "level");
        keywordMap.put("维修类别：", "category");
        keywordMap.put("发证日期：", "issueDate");
        keywordMap.put("有效期至：", "validUntil");
        return parseWithKeywordMap(ocrResult, keywordMap);
    }

    @Override
    public Map<String, String> parseEmergencyCert(String ocrResult) {
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("证书编号：", "certificateNumber");
        keywordMap.put("持证人姓名：", "name");
        keywordMap.put("身份证号：", "idNumber");
        keywordMap.put("培训项目：", "trainingItem");
        keywordMap.put("有效期：", "validPeriod");
        return parseWithKeywordMap(ocrResult, keywordMap);
    }

    @Override
    public Map<String, String> parseLicensePlate(String ocrResult) {
        Map<String, String> result = new HashMap<>();
        List<Map<String, Object>> wordsResult = safeParseJson(ocrResult);
        
        for (Map<String, Object> wordItem : wordsResult) {
            safeProcessLicensePlateItem(wordItem, result);
        }
        return result;
    }
    
    /**
     * 安全处理车牌识别条目
     */
    private void safeProcessLicensePlateItem(Map<String, Object> wordItem, Map<String, String> result) {
        try {
            String words = (String) wordItem.get("words");
            if (words == null) {
                return;
            }
            
            // 提取车牌号码
            if (words.matches("[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z0-9]{5,10}")) {
                result.put("plateNumber", words.trim());
            }
        } catch (Exception e) {
            log.error("解析车牌条目失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> parseVehicleLicenseFront(String ocrResult) {
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("号牌号码：", "plateNumber");
        keywordMap.put("所有人：", "owner");
        keywordMap.put("住址：", "address");
        keywordMap.put("品牌型号：", "brandModel");
        keywordMap.put("车辆型号：", "vehicleModel");
        keywordMap.put("发动机号：", "engineNumber");
        keywordMap.put("车辆识别代号：", "vin");
        keywordMap.put("使用性质：", "useNature");
        keywordMap.put("注册日期：", "registerDate");
        keywordMap.put("发证日期：", "issueDate");
        return parseWithKeywordMap(ocrResult, keywordMap);
    }

    @Override
    public Map<String, String> parseVehicleLicenseBack(String ocrResult) {
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("核定载人数：", "seatingCapacity");
        keywordMap.put("总质量：", "totalMass");
        keywordMap.put("整备质量：", "curbWeight");
        keywordMap.put("核定载质量：", "ratedLoad");
        keywordMap.put("外廓尺寸：", "dimensions");
        keywordMap.put("备注：", "remarks");
        keywordMap.put("检验记录：", "inspectionRecord");
        return parseWithKeywordMap(ocrResult, keywordMap);
    }

    @Override
    public Map<String, String> parseGeneral(String ocrResult) {
        Map<String, String> result = new HashMap<>();
        List<Map<String, Object>> wordsResult = safeParseJson(ocrResult);
        
        try {
            StringBuilder allText = new StringBuilder();
            for (Map<String, Object> wordItem : wordsResult) {
                String words = (String) wordItem.get("words");
                if (words != null) {
                    allText.append(words).append("\n");
                }
            }
            result.put("text", allText.toString().trim());
            result.put("wordCount", String.valueOf(wordsResult.size()));
        } catch (Exception e) {
            log.error("解析通用文字识别结果失败: {}", e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Map<String, String> parse(String ocrResult, String certType) {
        try {
            return switch (certType.toLowerCase()) {
                case "idcard" -> parseIdCard(ocrResult);
                case "driving" -> parseDrivingLicense(ocrResult);
                case "repair" -> parseRepairCert(ocrResult);
                case "emergency" -> parseEmergencyCert(ocrResult);
                case "licenseplate" -> parseLicensePlate(ocrResult);
                case "vehiclelicensefront" -> parseVehicleLicenseFront(ocrResult);
                case "vehiclelicenseback" -> parseVehicleLicenseBack(ocrResult);
                case "general" -> parseGeneral(ocrResult);
                default -> {
                    log.warn("未知的证件类型: {}", certType.toLowerCase());
                    yield new HashMap<>();
                }
            };
        } catch (Exception e) {
            log.error("解析识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}