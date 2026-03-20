package com.klzw.common.file.handler;

import com.klzw.common.file.properties.BaiduAIProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 百度OCR识别结果解析器工厂
 * 根据useSpecialUrl参数选择合适的解析策略
 */
@Slf4j
@Component
public class BaiduOcrParserFactory {

    private final TemplateOcrParser templateOcrParser;
    private final BaiduAIProperties baiduAIProperties;

    public BaiduOcrParserFactory(TemplateOcrParser templateOcrParser, BaiduAIProperties baiduAIProperties) {
        this.templateOcrParser = templateOcrParser;
        this.baiduAIProperties = baiduAIProperties;
    }

    /**
     * 解析身份证识别结果
     * 根据useSpecialUrl配置选择合适的解析策略
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的身份证信息
     */
    public Map<String, String> parseIdCard(String ocrResult) {
        try {
            log.debug("解析身份证识别结果, useSpecialUrl: {}", baiduAIProperties.isUseSpecialUrl());
            return templateOcrParser.parseIdCard(ocrResult);
        } catch (Exception e) {
            log.error("解析身份证识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析驾驶证识别结果
     * 根据useSpecialUrl配置选择合适的解析策略
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的驾驶证信息
     */
    public Map<String, String> parseDrivingLicense(String ocrResult) {
        try {
            log.debug("解析驾驶证识别结果, useSpecialUrl: {}", baiduAIProperties.isUseSpecialUrl());
            return templateOcrParser.parseDrivingLicense(ocrResult);
        } catch (Exception e) {
            log.error("解析驾驶证识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析维修资格证识别结果
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的维修资格证信息
     */
    public Map<String, String> parseRepairCert(String ocrResult) {
        try {
            return templateOcrParser.parseRepairCert(ocrResult);
        } catch (Exception e) {
            log.error("解析维修资格证识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析应急救援证识别结果
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的应急救援证信息
     */
    public Map<String, String> parseEmergencyCert(String ocrResult) {
        try {
            return templateOcrParser.parseEmergencyCert(ocrResult);
        } catch (Exception e) {
            log.error("解析应急救援证识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析车牌识别结果
     * 始终使用专用API解析策略
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的车牌信息
     */
    public Map<String, String> parseLicensePlate(String ocrResult) {
        try {
            log.debug("解析车牌识别结果, 始终使用专用API解析策略");
            return templateOcrParser.parseLicensePlate(ocrResult);
        } catch (Exception e) {
            log.error("解析车牌识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析行驶证正面识别结果
     * 根据useSpecialUrl配置选择合适的解析策略
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的行驶证信息
     */
    public Map<String, String> parseVehicleLicenseFront(String ocrResult) {
        try {
            log.debug("解析行驶证正面识别结果, useSpecialUrl: {}", baiduAIProperties.isUseSpecialUrl());
            return templateOcrParser.parseVehicleLicenseFront(ocrResult);
        } catch (Exception e) {
            log.error("解析行驶证识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析行驶证反面识别结果
     * 根据useSpecialUrl配置选择合适的解析策略
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的行驶证反面信息
     */
    public Map<String, String> parseVehicleLicenseBack(String ocrResult) {
        try {
            log.debug("解析行驶证反面识别结果, useSpecialUrl: {}", baiduAIProperties.isUseSpecialUrl());
            return templateOcrParser.parseVehicleLicenseBack(ocrResult);
        } catch (Exception e) {
            log.error("解析行驶证反面识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析通用文字识别结果
     *
     * @param ocrResult OCR识别结果
     * @return 解析后的文字信息
     */
    public Map<String, String> parseGeneral(String ocrResult) {
        try {
            return templateOcrParser.parseGeneral(ocrResult);
        } catch (Exception e) {
            log.error("解析通用文字识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 解析所有类型的证件识别结果
     * 根据useSpecialUrl配置选择合适的解析策略
     *
     * @param ocrResult OCR识别结果
     * @param type      证件类型
     * @return 解析后的证件信息
     */
    public Map<String, String> parse(String ocrResult, String type) {
        try {
            log.debug("解析识别结果, 类型: {}, useSpecialUrl: {}", type, baiduAIProperties.isUseSpecialUrl());
            return templateOcrParser.parse(ocrResult, type);
        } catch (Exception e) {
            log.error("解析识别结果失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

}