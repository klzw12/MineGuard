package com.klzw.common.file.impl;

import com.klzw.common.file.service.OcrService;
import com.klzw.common.file.util.BaiduOcrUtils;
import com.klzw.common.file.handler.BaiduOcrParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * OCR服务实现类
 */
@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    private final BaiduOcrUtils baiduOcrUtils;
    private final BaiduOcrParserFactory baiduOcrParserFactory;

    public OcrServiceImpl(BaiduOcrUtils baiduOcrUtils, BaiduOcrParserFactory baiduOcrParserFactory) {
        this.baiduOcrUtils = baiduOcrUtils;
        this.baiduOcrParserFactory = baiduOcrParserFactory;
    }

    @Override
    public String recognizeIdCard(MultipartFile file) {
        log.info("识别身份证正面");
        return baiduOcrUtils.recognizeIdCard(file, "front");
    }

    @Override
    public String recognizeIdCard(byte[] imageData) {
        log.info("识别身份证正面（byte[]）");
        return baiduOcrUtils.recognizeIdCard(imageData, "front");
    }

    @Override
    public String recognizeIdCardBack(MultipartFile file) {
        log.info("识别身份证背面");
        return baiduOcrUtils.recognizeIdCard(file, "back");
    }

    @Override
    public String recognizeDrivingLicense(MultipartFile file) {
        log.info("识别驾驶证");
        return baiduOcrUtils.recognizeDrivingLicense(file);
    }

    @Override
    public String recognizeDrivingLicense(byte[] imageData) {
        log.info("识别驾驶证（byte[]）");
        return baiduOcrUtils.recognizeDrivingLicense(imageData);
    }

    @Override
    public String recognizeVehicleLicense(MultipartFile file) {
        log.info("识别行驶证正面");
        return baiduOcrUtils.recognizeVehicleLicense(file, "front");
    }

    @Override
    public String recognizeVehicleLicenseBack(MultipartFile file) {
        log.info("识别行驶证背面");
        return baiduOcrUtils.recognizeVehicleLicense(file, "back");
    }

    @Override
    public String recognizeLicensePlate(MultipartFile file) {
        log.info("识别车牌");
        return baiduOcrUtils.recognizeLicensePlate(file);
    }

    @Override
    public String recognizeEmergencyCert(MultipartFile file) {
        log.info("识别紧急救援资格证");
        return baiduOcrUtils.recognizeGeneral(file);
    }

    @Override
    public String recognizeEmergencyCert(byte[] imageData) {
        log.info("识别紧急救援资格证（byte[]）");
        return baiduOcrUtils.recognizeGeneral(imageData);
    }

    @Override
    public String recognizeRepairCert(MultipartFile file) {
        log.info("识别维修资格证");
        return baiduOcrUtils.recognizeGeneral(file);
    }

    @Override
    public String recognizeRepairCert(byte[] imageData) {
        log.info("识别维修资格证（byte[]）");
        return baiduOcrUtils.recognizeGeneral(imageData);
    }

    @Override
    public Map<String, String> parseIdCard(String ocrResult) {
        log.info("解析身份证识别结果");
        return baiduOcrParserFactory.parseIdCard(ocrResult);
    }

    @Override
    public Map<String, String> parseDrivingLicense(String ocrResult) {
        log.info("解析驾驶证识别结果");
        return baiduOcrParserFactory.parseDrivingLicense(ocrResult);
    }

    @Override
    public Map<String, String> parseRepairCert(String ocrResult) {
        log.info("解析维修资格证识别结果");
        return baiduOcrParserFactory.parseRepairCert(ocrResult);
    }

    @Override
    public Map<String, String> parseEmergencyCert(String ocrResult) {
        log.info("解析应急救援证识别结果");
        return baiduOcrParserFactory.parseEmergencyCert(ocrResult);
    }

    @Override
    public Map<String, String> parseLicensePlate(String ocrResult) {
        log.info("解析车牌识别结果");
        return baiduOcrParserFactory.parseLicensePlate(ocrResult);
    }

    @Override
    public Map<String, String> parseVehicleLicenseFront(String ocrResult) {
        log.info("解析行驶证正面识别结果");
        return baiduOcrParserFactory.parseVehicleLicenseFront(ocrResult);
    }

    @Override
    public Map<String, String> parseVehicleLicenseBack(String ocrResult) {
        log.info("解析行驶证背面识别结果");
        return baiduOcrParserFactory.parseVehicleLicenseBack(ocrResult);
    }

    @Override
    public Map<String, String> parseGeneral(String ocrResult) {
        log.info("解析通用文字识别结果");
        return baiduOcrParserFactory.parseGeneral(ocrResult);
    }

    @Override
    public Map<String, String> parse(String ocrResult, String certType) {
        log.info("解析识别结果, 类型: {}", certType);
        return baiduOcrParserFactory.parse(ocrResult, certType);
    }

}
