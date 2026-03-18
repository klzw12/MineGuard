package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 身份证验证DTO
 * <p>
 * 用于身份证实名认证，验证姓名和身份证号
 * 前置条件：用户必须先完成身份证验证才能上传资格证书
 */
@Data
public class IdCardVerifyDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名（必填）
     */
    private String realName;

    /**
     * 身份证号（必填）
     */
    private String idCard;

    /**
     * 身份证正面图片Base64（必填）
     */
    private String idCardFrontBase64;

    /**
     * 身份证背面图片Base64（可选）
     */
    private String idCardBackBase64;
}
