package com.klzw.common.file.util;

import com.alibaba.fastjson.JSONObject;
import com.klzw.common.file.properties.BaiduAIProperties;
import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.constant.FileResultCode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BaiduOcrUtils {

    private final BaiduAIProperties baiduAIProperties;
    private final OkHttpClient httpClient;
    private String cachedAccessToken;
    private long tokenExpireTime;

    public BaiduOcrUtils(BaiduAIProperties baiduAIProperties) {
        this.baiduAIProperties = baiduAIProperties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取百度云API访问令牌（带缓存）
     * @return 访问令牌
     */
    public String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" +
                baiduAIProperties.getApiKey() + "&client_secret=" + baiduAIProperties.getSecretKey();
        try {
            Request request = new Request.Builder().url(url).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "获取访问令牌失败: " + response.code());
                }
                String responseBody = response.body().string();
                log.debug("百度云API响应: {}", responseBody);

                JSONObject jsonObject = JSONObject.parseObject(responseBody);
                String accessToken = jsonObject.getString("access_token");
                if (accessToken == null) {
                    log.error("获取百度OCR AK失败");
                    throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "百度云API访问令牌为空");
                }
                log.info("成功获取百度云API访问令牌");
                cachedAccessToken = accessToken;
                tokenExpireTime = System.currentTimeMillis() + 2592000000L;
                return accessToken;
            }
        } catch (Exception e) {
            String errorMsg = "获取百度云访问令牌失败: " + e.getMessage();
            log.error(errorMsg, e);
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, errorMsg);
        }
    }

    /**
     * 统一的识别方法
     * @param apiUrl API地址
     * @param encodedImage 已编码的图片数据
     * @param extraParams 额外参数
     * @return 识别结果
     */
    private String recognize(String apiUrl, String encodedImage, Map<String, Object> extraParams) {
        String accessToken = getAccessToken();
        String url = apiUrl + "?access_token=" + accessToken;
        log.debug("识别URL: {}", url);

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("image", encodedImage);
        if (extraParams != null) {
            for (Map.Entry<String, Object> entry : extraParams.entrySet()) {
                formBuilder.add(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String result = response.body().string();
            log.info("识别成功");
            log.debug("识别结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("识别失败, 错误: {}", e.getMessage(), e);
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "识别失败: " + e.getMessage());
        }
    }

    /**
     * 通用识别方法（byte[]输入）
     * @param imageData 图片二进制数据
     * @param specialUrl 特定API URL
     * @param params 额外参数
     * @param logMessage 日志信息
     * @return 识别结果
     */
    private String recognize(byte[] imageData, String specialUrl, Map<String, Object> params, String logMessage) {
        log.info(logMessage);
        try {
            String encodedImage = imageConvert(imageData);
            String url = baiduAIProperties.isUseSpecialUrl() ? specialUrl : baiduAIProperties.getGeneralUrl();
            return recognize(url, encodedImage, params);
        } catch (IOException e) {
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 通用识别方法（File输入）
     * @param file 图片文件
     * @param specialUrl 特定API URL
     * @param params 额外参数
     * @param logMessage 日志信息
     * @return 识别结果
     */
    private String recognize(File file, String specialUrl, Map<String, Object> params, String logMessage) {
        log.info(logMessage);
        try {
            String encodedImage = imageConvert(file.getAbsolutePath());
            String url = baiduAIProperties.isUseSpecialUrl() ? specialUrl : baiduAIProperties.getGeneralUrl();
            return recognize(url, encodedImage, params);
        } catch (IOException e) {
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 通用识别方法（MultipartFile输入）
     * @param file 上传的图片文件
     * @param specialUrl 特定API URL
     * @param params 额外参数
     * @param logMessage 日志信息
     * @return 识别结果
     */
    private String recognize(MultipartFile file, String specialUrl, Map<String, Object> params, String logMessage) {
        log.info(logMessage);
        try {
            String encodedImage = imageConvert(file);
            String url = baiduAIProperties.isUseSpecialUrl() ? specialUrl : baiduAIProperties.getGeneralUrl();
            return recognize(url, encodedImage, params);
        } catch (IOException e) {
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 身份证识别（byte[]输入）
     * @param imageData 图片二进制数据
     * @param idCardSide 身份证正反面，front：正面，back：反面
     * @return 识别结果
     */
    public String recognizeIdCard(byte[] imageData, String idCardSide) {
        Map<String, Object> params = new HashMap<>();
        params.put("id_card_side", idCardSide);
        return recognize(imageData, baiduAIProperties.getIdcardUrl(), params, "开始身份证识别, 正反面: " + idCardSide);
    }

    /**
     * 身份证识别（File输入）
     * @param file 图片文件
     * @param idCardSide 身份证正反面，front：正面，back：反面
     * @return 识别结果
     */
    public String recognizeIdCard(File file, String idCardSide) {
        Map<String, Object> params = new HashMap<>();
        params.put("id_card_side", idCardSide);
        return recognize(file, baiduAIProperties.getIdcardUrl(), params, "开始身份证识别, 正反面: " + idCardSide);
    }

    /**
     * 身份证识别（MultipartFile输入）
     * @param file 上传的图片文件
     * @param idCardSide 身份证正反面，front：正面，back：反面
     * @return 识别结果
     */
    public String recognizeIdCard(MultipartFile file, String idCardSide) {
        Map<String, Object> params = new HashMap<>();
        params.put("id_card_side", idCardSide);
        return recognize(file, baiduAIProperties.getIdcardUrl(), params, "开始身份证识别, 正反面: " + idCardSide);
    }

    /**
     * 通用文字识别（byte[]输入）
     * @param imageData 图片二进制数据
     * @return 识别结果
     */
    public String recognizeGeneral(byte[] imageData) {
        return recognize(imageData, baiduAIProperties.getGeneralUrl(), null, "开始通用文字识别");
    }

    /**
     * 通用文字识别（File输入）
     * @param file 图片文件
     * @return 识别结果
     */
    public String recognizeGeneral(File file) {
        return recognize(file, baiduAIProperties.getGeneralUrl(), null, "开始通用文字识别");
    }

    /**
     * 通用文字识别（MultipartFile输入）
     * @param file 上传的图片文件
     * @return 识别结果
     */
    public String recognizeGeneral(MultipartFile file) {
        return recognize(file, baiduAIProperties.getGeneralUrl(), null, "开始通用文字识别");
    }

    /**
     * 驾驶证识别（byte[]输入）
     * @param imageData 图片二进制数据
     * @return 识别结果
     */
    public String recognizeDrivingLicense(byte[] imageData) {
        return recognize(imageData, baiduAIProperties.getDrivingLicenseUrl(), null, "开始驾驶证识别");
    }

    /**
     * 驾驶证识别（File输入）
     * @param file 图片文件
     * @return 识别结果
     */
    public String recognizeDrivingLicense(File file) {
        return recognize(file, baiduAIProperties.getDrivingLicenseUrl(), null, "开始驾驶证识别");
    }

    /**
     * 驾驶证识别（MultipartFile输入）
     * @param file 上传的图片文件
     * @return 识别结果
     */
    public String recognizeDrivingLicense(MultipartFile file) {
        return recognize(file, baiduAIProperties.getDrivingLicenseUrl(), null, "开始驾驶证识别");
    }

    /**
     * 行驶证识别（byte[]输入）
     * @param imageData 图片二进制数据
     * @param vehicleLicenseSide 行驶证正反面，front：正面，back：反面
     * @return 识别结果
     */
    public String recognizeVehicleLicense(byte[] imageData, String vehicleLicenseSide) {
        Map<String, Object> params = new HashMap<>();
        params.put("vehicle_license_side", vehicleLicenseSide);
        return recognize(imageData, baiduAIProperties.getVehicleLicenseUrl(), params, "开始行驶证识别, 正反面: " + vehicleLicenseSide);
    }

    /**
     * 行驶证识别（File输入）
     * @param file 图片文件
     * @param vehicleLicenseSide 行驶证正反面，front：正面，back：反面
     * @return 识别结果
     */
    public String recognizeVehicleLicense(File file, String vehicleLicenseSide) {
        Map<String, Object> params = new HashMap<>();
        params.put("vehicle_license_side", vehicleLicenseSide);
        return recognize(file, baiduAIProperties.getVehicleLicenseUrl(), params, "开始行驶证识别, 正反面: " + vehicleLicenseSide);
    }

    /**
     * 行驶证识别（MultipartFile输入）
     * @param file 上传的图片文件
     * @param vehicleLicenseSide 行驶证正反面，front：正面，back：反面
     * @return 识别结果
     */
    public String recognizeVehicleLicense(MultipartFile file, String vehicleLicenseSide) {
        Map<String, Object> params = new HashMap<>();
        params.put("vehicle_license_side", vehicleLicenseSide);
        return recognize(file, baiduAIProperties.getVehicleLicenseUrl(), params, "开始行驶证识别, 正反面: " + vehicleLicenseSide);
    }

    /**
     * 车牌识别（byte[]输入）
     * @param imageData 图片二进制数据
     * @return 识别结果
     */
    public String recognizeLicensePlate(byte[] imageData) {
        return recognize(imageData, baiduAIProperties.getLicensePlateUrl(), null, "开始车牌识别");
    }

    /**
     * 车牌识别（File输入）
     * @param file 图片文件
     * @return 识别结果
     */
    public String recognizeLicensePlate(File file) {
        return recognize(file, baiduAIProperties.getLicensePlateUrl(), null, "开始车牌识别");
    }

    /**
     * 车牌识别（MultipartFile输入）
     * @param file 上传的图片文件
     * @return 识别结果
     */
    public String recognizeLicensePlate(MultipartFile file) {
        log.info("useSpecialUrl: {}, licensePlateUrl: {}, generalUrl: {}", 
                baiduAIProperties.isUseSpecialUrl(), 
                baiduAIProperties.getLicensePlateUrl(), 
                baiduAIProperties.getGeneralUrl());
        String result = recognize(file, baiduAIProperties.getLicensePlateUrl(), null, "开始车牌识别");
        log.info("实际使用的URL: {}", baiduAIProperties.isUseSpecialUrl() ? baiduAIProperties.getLicensePlateUrl() : baiduAIProperties.getGeneralUrl());
        return result;
    }

    /**
     * 图片转换：将MultipartFile转换为base64编码并验证
     * @param file 上传的图片文件
     * @return base64编码后的图片数据
     * @throws IOException IO异常
     */
    public String imageConvert(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }
        byte[] imageBytes = file.getBytes();
        validateImageData(imageBytes);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 从本地文件路径读取图片并转换为base64编码
     * @param filePath 本地文件路径
     * @return base64编码后的图片数据
     * @throws IOException IO异常
     */
    public String imageConvert(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("路径不是文件: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] imageBytes = fis.readAllBytes();
            validateImageData(imageBytes);
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    /**
     * 从byte数组读取图片并转换为base64编码
     * @param imageData 图片二进制数据
     * @return base64编码后的图片数据
     * @throws IOException IO异常
     */
    public String imageConvert(byte[] imageData) throws IOException {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        validateImageData(imageData);

        return Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * 验证图片数据是否符合百度AI API要求
     * @param imageData 图片二进制数据
     * @throws IOException IO异常
     */
    private void validateImageData(byte[] imageData) throws IOException {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        long maxSize = 8 * 1024 * 1024;
        if (imageData.length > maxSize) {
            throw new IllegalArgumentException("图片大小不能超过8MB，当前大小: " + (imageData.length / 1024.0 / 1024) + "MB");
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                throw new IllegalArgumentException("图片格式不支持或文件已损坏");
            }
            int width = image.getWidth();
            int height = image.getHeight();
            int minSize = 15;
            int maxSizeDimension = 4096;
            
            if (width < minSize || height < minSize) {
                throw new IllegalArgumentException("图片最短边至少" + minSize + "px，当前尺寸: " + width + "x" + height);
            }
            if (width > maxSizeDimension || height > maxSizeDimension) {
                throw new IllegalArgumentException("图片最长边最大" + maxSizeDimension + "px，当前尺寸: " + width + "x" + height);
            }

            String formatName = getImageFormatName(imageData);
            if (!isValidImageFormat(formatName)) {
                throw new IllegalArgumentException("仅支持jpg/jpeg/png/bmp格式的图片，当前格式: " + formatName);
            }
        } catch (IOException e) {
            throw new IOException("图片验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取图片格式名称
     * @param imageData 图片二进制数据
     * @return 图片格式名称
     */
    private String getImageFormatName(byte[] imageData) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        if (image == null) {
            return "unknown";
        }

        String[] formatNames = ImageIO.getReaderFormatNames();
        for (String format : formatNames) {
            if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg") ||
                    format.equalsIgnoreCase("png") || format.equalsIgnoreCase("bmp")) {
                return format.toLowerCase();
            }
        }
        return "unknown";
    }

    /**
     * 验证图片格式是否有效
     * @param formatName 图片格式名称
     * @return 是否为有效格式
     */
    private boolean isValidImageFormat(String formatName) {
        if (formatName == null) {
            return false;
        }
        String lowerFormat = formatName.toLowerCase();
        return lowerFormat.equals("jpg") ||
                lowerFormat.equals("jpeg") ||
                lowerFormat.equals("png") ||
                lowerFormat.equals("bmp");
    }
}
