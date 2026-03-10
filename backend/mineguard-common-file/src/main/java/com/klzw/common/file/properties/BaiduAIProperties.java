package com.klzw.common.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.file.ocr.baidu")
public class BaiduAIProperties {
    private boolean enabled = false;
    private String apiKey;
    private String secretKey;
    private int connectionTimeout = 10000;
    private int readTimeout = 30000;
    private boolean useSpecialUrl = false;
    private String licensePlateUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate";
    private String generalUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    private String idCardUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard";
    private String drivingLicenseUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/driving_license";
    private String vehicleLicenseUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/vehicle_license";
}
