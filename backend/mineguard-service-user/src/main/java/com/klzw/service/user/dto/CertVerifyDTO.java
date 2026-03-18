package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 资格证书验证DTO
 * <p>
 * 用于上传资格证书进行资格认证
 * 前置条件：用户必须先完成身份证验证（实名认证）
 * 系统会根据userId查询已验证的真实姓名进行比对
 */
@Data
public class CertVerifyDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 人员类型：1-司机，2-安全员，3-维修员
     */
    private Integer personType;

    /**
     * 证书编号（必填）
     */
    private String certNumber;

    /**
     * 驾驶证图片Base64（司机必填）
     */
    private String drivingLicenseBase64;

    /**
     * 应急救援证图片Base64（安全员必填）
     */
    private String emergencyCertBase64;

    /**
     * 维修资格证图片Base64（维修员必填）
     */
    private String repairCertBase64;
}
