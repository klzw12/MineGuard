package com.klzw.common.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * OCR服务接口
 */
public interface OcrService {

    /**
     * 识别身份证正面
     * @param file 身份证正面图片
     * @return 识别结果
     */
    String recognizeIdCard(MultipartFile file);
    
    /**
     * 识别身份证正面（byte[]输入）
     * @param imageData 图片二进制数据
     * @return 识别结果
     */
    String recognizeIdCard(byte[] imageData);

    /**
     * 识别身份证背面
     * @param file 身份证背面图片
     * @return 识别结果
     */
    String recognizeIdCardBack(MultipartFile file);

    /**
     * 识别驾驶证
     * @param file 驾驶证图片
     * @return 识别结果
     */
    String recognizeDrivingLicense(MultipartFile file);
    
    /**
     * 识别驾驶证（byte[]输入）
     * @param imageData 图片二进制数据
     * @return 识别结果
     */
    String recognizeDrivingLicense(byte[] imageData);

    /**
     * 识别行驶证正面
     * @param file 行驶证正面图片
     * @return 识别结果
     */
    String recognizeVehicleLicense(MultipartFile file);

    /**
     * 识别行驶证背面
     * @param file 行驶证背面图片
     * @return 识别结果
     */
    String recognizeVehicleLicenseBack(MultipartFile file);

    /**
     * 识别车牌
     * @param file 车牌图片
     * @return 识别结果
     */
    String recognizeLicensePlate(MultipartFile file);

    /**
     * 识别紧急救援资格证
     * @param file 紧急救援资格证图片
     * @return 识别结果
     */
    String recognizeEmergencyCert(MultipartFile file);
    
    /**
     * 识别紧急救援资格证（byte[]输入）
     * @param imageData 图片二进制数据
     * @return 识别结果
     */
    String recognizeEmergencyCert(byte[] imageData);

    /**
     * 识别维修资格证
     * @param file 维修资格证图片
     * @return 识别结果
     */
    String recognizeRepairCert(MultipartFile file);
    
    /**
     * 识别维修资格证（byte[]输入）
     * @param imageData 图片二进制数据
     * @return 识别结果
     */
    String recognizeRepairCert(byte[] imageData);

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