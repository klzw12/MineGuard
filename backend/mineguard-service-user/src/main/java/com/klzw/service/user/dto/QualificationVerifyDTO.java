package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 资格验证DTO
 * <p>
 * 用于传递人员资格验证所需的信息
 */
@Data
public class QualificationVerifyDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 人员类型：1-司机，2-安全员，3-维修员
     */
    private Integer personType;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证正面图片Base64
     */
    private String idCardFrontBase64;

    /**
     * 身份证背面图片Base64
     */
    private String idCardBackBase64;

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

    /**
     * 证书编号
     */
    private String certNumber;

    /**
     * 证书有效期
     */
    private String validUntil;
}
