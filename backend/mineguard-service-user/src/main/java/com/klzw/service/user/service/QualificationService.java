package com.klzw.service.user.service;

import com.klzw.service.user.dto.CertVerifyDTO;
import com.klzw.service.user.dto.IdCardVerifyDTO;

/**
 * 人员资格验证服务接口
 * <p>
 * 用于处理司机、安全员、维修员等人员的资格认证
 * 流程：先完成身份证验证（实名认证），再上传资格证书
 */
public interface QualificationService {

    /**
     * 身份证验证（实名认证）
     * <p>
     * 验证用户提交的身份证信息，OCR识别后比对姓名和身份证号
     * 验证通过后会更新用户的realName字段
     * 
     * @param dto 身份证验证DTO
     * @return 验证结果
     */
    boolean verifyIdCard(IdCardVerifyDTO dto);

    /**
     * 检查用户是否已完成身份证验证
     * 
     * @param userId 用户ID
     * @return true-已验证，false-未验证
     */
    boolean checkIdCardVerified(Long userId);

    /**
     * 上传资格证书（司机）
     * <p>
     * 前置条件：用户必须先完成身份证验证
     * 系统会根据userId查询已验证的真实姓名进行比对
     * 
     * @param dto 证书验证DTO
     * @return 验证结果
     */
    boolean uploadDriverCert(CertVerifyDTO dto);

    /**
     * 上传资格证书（安全员）
     * <p>
     * 前置条件：用户必须先完成身份证验证
     * 系统会根据userId查询已验证的真实姓名进行比对
     * 
     * @param dto 证书验证DTO
     * @return 验证结果
     */
    boolean uploadSafetyOfficerCert(CertVerifyDTO dto);

    /**
     * 上传资格证书（维修员）
     * <p>
     * 前置条件：用户必须先完成身份证验证
     * 系统会根据userId查询已验证的真实姓名进行比对
     * 
     * @param dto 证书验证DTO
     * @return 验证结果
     */
    boolean uploadRepairmanCert(CertVerifyDTO dto);
}
