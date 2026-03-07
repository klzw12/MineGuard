package com.klzw.common.file.handler;

import java.util.Map;

/**
 * OCR解析器接口
 * 定义OCR识别结果解析的通用方法
 */
public interface OcrParser {

    /**
     * 解析身份证识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的身份证信息
     */
    Map<String, String> parseIdCard(String ocrResult);

    /**
     * 解析驾驶证识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的驾驶证信息
     */
    Map<String, String> parseDrivingLicense(String ocrResult);

    /**
     * 解析维修资格证识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的维修资格证信息
     */
    Map<String, String> parseRepairCert(String ocrResult);

    /**
     * 解析应急救援证识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的应急救援证信息
     */
    Map<String, String> parseEmergencyCert(String ocrResult);

    /**
     * 解析车牌识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的车牌信息
     */
    Map<String, String> parseLicensePlate(String ocrResult);

    /**
     * 解析行驶证正面识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的行驶证信息
     */
    Map<String, String> parseVehicleLicenseFront(String ocrResult);

    /**
     * 解析行驶证反面识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的行驶证反面信息
     */
    Map<String, String> parseVehicleLicenseBack(String ocrResult);

    /**
     * 解析通用文字识别结果
     * @param ocrResult OCR识别结果
     * @return 解析后的文字信息
     */
    Map<String, String> parseGeneral(String ocrResult);

    /**
     * 解析所有类型的证件识别结果
     * @param ocrResult OCR识别结果
     * @param certType 证件类型
     * @return 解析后的证件信息
     */
    Map<String, String> parse(String ocrResult, String certType);

}