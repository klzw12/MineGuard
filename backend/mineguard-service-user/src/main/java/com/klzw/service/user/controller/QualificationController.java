package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.CertVerifyDTO;
import com.klzw.service.user.dto.IdCardVerifyDTO;
import com.klzw.service.user.service.QualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 资格认证控制器
 * <p>
 * 用于处理用户的实名认证和资格认证
 * 流程：先完成身份证验证（实名认证），再上传资格证书
 */
@Tag(name = "资格认证", description = "用户实名认证和资格认证")
@RestController
@RequestMapping("/qualification")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationService qualificationService;

    @Operation(summary = "身份证验证（实名认证）")
    @PostMapping("/idcard/verify")
    public Result<Boolean> verifyIdCard(@Valid @RequestBody IdCardVerifyDTO dto) {
        boolean result = qualificationService.verifyIdCard(dto);
        return Result.success("身份证验证成功", result);
    }

    @Operation(summary = "检查用户是否已完成身份证验证")
    @GetMapping("/idcard/check/{userId}")
    public Result<Boolean> checkIdCardVerified(@PathVariable Long userId) {
        boolean result = qualificationService.checkIdCardVerified(userId);
        return Result.success(result);
    }

    @Operation(summary = "上传驾驶证（司机资格认证）")
    @PostMapping("/cert/driver")
    public Result<Boolean> uploadDriverCert(@Valid @RequestBody CertVerifyDTO dto) {
        boolean result = qualificationService.uploadDriverCert(dto);
        return Result.success("驾驶证上传成功", result);
    }

    @Operation(summary = "上传应急救援证（安全员资格认证）")
    @PostMapping("/cert/safety-officer")
    public Result<Boolean> uploadSafetyOfficerCert(@Valid @RequestBody CertVerifyDTO dto) {
        boolean result = qualificationService.uploadSafetyOfficerCert(dto);
        return Result.success("应急救援证上传成功", result);
    }

    @Operation(summary = "上传维修资格证（维修员资格认证）")
    @PostMapping("/cert/repairman")
    public Result<Boolean> uploadRepairmanCert(@Valid @RequestBody CertVerifyDTO dto) {
        boolean result = qualificationService.uploadRepairmanCert(dto);
        return Result.success("维修资格证上传成功", result);
    }
}
