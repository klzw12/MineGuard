package com.klzw.service.user.service.impl;

import com.klzw.common.core.enums.UserStatusEnum;
import com.klzw.common.file.service.OcrService;
import com.klzw.common.file.service.StorageService;
import com.klzw.common.auth.util.PasswordUtils;
import com.klzw.service.user.dto.CertVerifyDTO;
import com.klzw.service.user.dto.IdCardVerifyDTO;
import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.entity.RoleChangeApply;
import com.klzw.service.user.entity.SafetyOfficer;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.entity.Repairman;
import com.klzw.service.user.exception.UserException;
import com.klzw.service.user.constant.UserResultCode;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.mapper.RepairmanMapper;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.mapper.SafetyOfficerMapper;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.service.QualificationService;
import com.klzw.service.user.service.RoleChangeApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualificationServiceImpl implements QualificationService {

    private final StorageService storageService;
    private final OcrService ocrService;
    private final DriverMapper driverMapper;
    private final SafetyOfficerMapper safetyOfficerMapper;
    private final RepairmanMapper repairmanMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final RoleChangeApplyService roleChangeApplyService;
    private final PasswordUtils passwordUtils;
    
    private static final String ROLE_DRIVER = "ROLE_DRIVER";
    private static final String ROLE_SAFETY_OFFICER = "ROLE_SAFETY_OFFICER";
    private static final String ROLE_REPAIRMAN = "ROLE_REPAIRMAN";
    
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    @Override
    @Transactional
    public boolean verifyIdCard(IdCardVerifyDTO dto) {
        log.info("身份证验证，用户ID：{}，姓名：{}", dto.getUserId(), dto.getRealName());
        
        User user = validateUserExists(dto.getUserId());
        validateIdCardFormat(dto.getIdCard());
        
        String idCardFrontUrl = uploadImage(dto.getIdCardFrontBase64(), "idcard_front");
        Map<String, String> idCardInfo = ocrService.parseIdCard(idCardFrontUrl);
        
        validateIdCardInfo(idCardInfo, dto.getRealName(), dto.getIdCard());
        
        String idCardBackUrl = uploadOptionalImage(dto.getIdCardBackBase64(), "idcard_back");
        
        user.setIdCardFrontUrl(idCardFrontUrl);
        user.setIdCardBackUrl(idCardBackUrl);
        
        user.setRealName(dto.getRealName());
        user.setIdCard(passwordUtils.encodeIdCard(dto.getIdCard()));
        user.setGender(parseGender(idCardInfo.get("gender")));
        user.setNation(idCardInfo.get("nation"));
        user.setBirthDate(idCardInfo.get("birth"));
        user.setAddress(idCardInfo.get("address"));
        userMapper.updateById(user);
        
        log.info("身份证验证成功，用户ID：{}，姓名：{}", dto.getUserId(), dto.getRealName());
        return true;
    }

    private Integer parseGender(String gender) {
        if (gender == null) {
            return null;
        }
        return "男".equals(gender) ? 1 : "女".equals(gender) ? 2 : 0;
    }

    @Override
    public boolean checkIdCardVerified(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }
        return user.getRealName() != null && !user.getRealName().isEmpty();
    }

    @Override
    @Transactional
    public boolean uploadDriverCert(CertVerifyDTO dto) {
        log.info("上传驾驶证，用户ID：{}", dto.getUserId());
        
        User user = validateUserExists(dto.getUserId());
        validateIdCardVerified(user);
        
        String drivingLicenseUrl = uploadImage(dto.getDrivingLicenseBase64(), "driving_license");
        Map<String, String> licenseInfo = ocrService.parseDrivingLicense(drivingLicenseUrl);
        
        validateDrivingLicense(licenseInfo, user.getRealName());
        validateLicenseNotExpired(licenseInfo);
        
        saveDriverCert(dto, user, drivingLicenseUrl, licenseInfo);
        assignRoleToUser(dto.getUserId(), ROLE_DRIVER);
        
        log.info("驾驶证上传成功，用户ID：{}", dto.getUserId());
        return true;
    }

    @Override
    @Transactional
    public boolean uploadSafetyOfficerCert(CertVerifyDTO dto) {
        log.info("上传应急救援证，用户ID：{}", dto.getUserId());
        
        User user = validateUserExists(dto.getUserId());
        validateIdCardVerified(user);
        
        String emergencyCertUrl = uploadImage(dto.getEmergencyCertBase64(), "emergency_cert");
        Map<String, String> certInfo = ocrService.parseEmergencyCert(emergencyCertUrl);
        
        validateEmergencyCert(certInfo, user.getRealName());
        validateCertNotExpired(certInfo);
        
        saveSafetyOfficerCert(dto, user, emergencyCertUrl, certInfo);
        assignRoleToUser(dto.getUserId(), ROLE_SAFETY_OFFICER);
        
        log.info("应急救援证上传成功，用户ID：{}", dto.getUserId());
        return true;
    }

    @Override
    @Transactional
    public boolean uploadRepairmanCert(CertVerifyDTO dto) {
        log.info("上传维修资格证，用户ID：{}", dto.getUserId());
        
        User user = validateUserExists(dto.getUserId());
        validateIdCardVerified(user);
        
        String repairCertUrl = uploadImage(dto.getRepairCertBase64(), "repair_cert");
        Map<String, String> certInfo = ocrService.parseRepairCert(repairCertUrl);
        
        validateRepairCert(certInfo, user.getRealName());
        validateCertNotExpired(certInfo);
        
        saveRepairmanCert(dto, user, repairCertUrl, certInfo);
        assignRoleToUser(dto.getUserId(), ROLE_REPAIRMAN);
        
        log.info("维修资格证上传成功，用户ID：{}", dto.getUserId());
        return true;
    }
    
    private User validateUserExists(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(UserResultCode.USER_NOT_FOUND);
        }
        return user;
    }
    
    private void validateIdCardVerified(User user) {
        if (user.getRealName() == null || user.getRealName().isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_NOT_VERIFIED, "请先完成身份证验证（实名认证）");
        }
    }
    
    private void validateIdCardFormat(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            throw new UserException(UserResultCode.PARAM_ERROR, "身份证号不能为空");
        }
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证号格式不正确");
        }
    }
    
    private void validateIdCardInfo(Map<String, String> idCardInfo, String expectedName, String expectedIdCard) {
        if (idCardInfo == null || idCardInfo.isEmpty()) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证识别失败");
        }
        
        String actualName = idCardInfo.get("name");
        String actualIdCard = idCardInfo.get("idNumber");
        
        if (actualName == null || actualName.isEmpty()) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证识别失败：无法识别姓名");
        }
        if (actualIdCard == null || actualIdCard.isEmpty()) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证识别失败：无法识别身份证号");
        }
        
        if (!actualName.equals(expectedName)) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证姓名与提交姓名不一致");
        }
        if (!actualIdCard.equalsIgnoreCase(expectedIdCard)) {
            throw new UserException(UserResultCode.ID_CARD_INVALID, "身份证号与提交的身份证号不一致");
        }
    }
    
    private void validateDrivingLicense(Map<String, String> licenseInfo, String expectedName) {
        if (licenseInfo == null || licenseInfo.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "驾驶证识别失败");
        }
        
        String licenseName = licenseInfo.get("name");
        String licenseType = licenseInfo.get("准驾车型");
        
        if (licenseName == null || licenseName.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "驾驶证识别失败：无法识别姓名");
        }
        if (!licenseName.equals(expectedName)) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "驾驶证姓名与实名认证姓名不一致");
        }
        if (licenseType == null || licenseType.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "驾驶证识别失败：无法识别准驾车型");
        }
    }
    
    private void validateEmergencyCert(Map<String, String> certInfo, String expectedName) {
        if (certInfo == null || certInfo.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "应急救援证识别失败");
        }
        
        String certName = certInfo.get("name");
        String certNumber = certInfo.get("certificateNumber");
        
        if (certName == null || certName.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "应急救援证识别失败：无法识别姓名");
        }
        if (!certName.equals(expectedName)) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "应急救援证姓名与实名认证姓名不一致");
        }
        if (certNumber == null || certNumber.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "应急救援证识别失败：无法识别证书编号");
        }
    }
    
    private void validateRepairCert(Map<String, String> certInfo, String expectedName) {
        if (certInfo == null || certInfo.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "维修资格证识别失败");
        }
        
        String certName = certInfo.get("name");
        String certNumber = certInfo.get("certificateNumber");
        
        if (certName == null || certName.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "维修资格证识别失败：无法识别姓名");
        }
        if (!certName.equals(expectedName)) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "维修资格证姓名与实名认证姓名不一致");
        }
        if (certNumber == null || certNumber.isEmpty()) {
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "维修资格证识别失败：无法识别证书编号");
        }
    }
    
    private void validateLicenseNotExpired(Map<String, String> licenseInfo) {
        String validTo = licenseInfo.get("validTo");
        
        if (validTo != null && !validTo.isEmpty()) {
            LocalDate expiryDate = parseDate(validTo);
            if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
                throw new UserException(UserResultCode.CERTIFICATE_EXPIRED, "驾驶证已过期");
            }
        }
    }
    
    private void validateCertNotExpired(Map<String, String> certInfo) {
        String validUntil = certInfo.get("validUntil");
        
        if (validUntil != null && !validUntil.isEmpty()) {
            LocalDate expiryDate = parseDate(validUntil);
            if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
                throw new UserException(UserResultCode.CERTIFICATE_EXPIRED, "资格证书已过期");
            }
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        String normalized = dateStr.replace("/", "-").replace("年", "-").replace("月", "-").replace("日", "");
        
        try {
            return LocalDate.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ignored) {
        }
        
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception ignored) {
        }
        
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void saveDriverCert(CertVerifyDTO dto, User user, String drivingLicenseUrl, Map<String, String> licenseInfo) {
        Driver existingDriver = driverMapper.selectByUserId(String.valueOf(dto.getUserId()));
        
        Driver driver = new Driver();
        driver.setUserId(dto.getUserId());
        driver.setDriverName(user.getRealName());
        driver.setIdCard(user.getIdCard());
        driver.setIdCardFrontUrl(existingDriver != null ? existingDriver.getIdCardFrontUrl() : null);
        driver.setIdCardBackUrl(existingDriver != null ? existingDriver.getIdCardBackUrl() : null);
        driver.setDrivingLicenseUrl(drivingLicenseUrl);
        driver.setStatus(UserStatusEnum.ENABLED.getValue());
        
        if (licenseInfo != null) {
            driver.setLicenseType(licenseInfo.get("准驾车型"));
            String gender = licenseInfo.get("gender");
            if (gender != null) {
                driver.setGender(parseGender(gender));
            }
            driver.setAddress(licenseInfo.get("address"));
            driver.setBirthDate(licenseInfo.get("birth"));
            driver.setFirstIssueDate(licenseInfo.get("firstIssueDate"));
            driver.setValidPeriod(licenseInfo.get("validPeriod"));
            driver.setLicenseNumber(licenseInfo.get("licenseNumber"));
        }
        
        if (existingDriver != null) {
            driver.setId(existingDriver.getId());
            driverMapper.updateById(driver);
        } else {
            driverMapper.insert(driver);
        }
        
        log.info("司机资格信息已保存，用户ID：{}", dto.getUserId());
    }
    
    private void saveSafetyOfficerCert(CertVerifyDTO dto, User user, String emergencyCertUrl, Map<String, String> certInfo) {
        SafetyOfficer existingOfficer = safetyOfficerMapper.selectByUserId(String.valueOf(dto.getUserId()));
        
        SafetyOfficer officer = new SafetyOfficer();
        officer.setUserId(dto.getUserId());
        officer.setOfficerName(user.getRealName());
        officer.setIdCard(user.getIdCard());
        officer.setIdCardFrontUrl(existingOfficer != null ? existingOfficer.getIdCardFrontUrl() : null);
        officer.setIdCardBackUrl(existingOfficer != null ? existingOfficer.getIdCardBackUrl() : null);
        officer.setEmergencyCertUrl(emergencyCertUrl);
        officer.setStatus(UserStatusEnum.ENABLED.getValue());
        
        if (certInfo != null) {
            officer.setCertNumber(dto.getCertNumber() != null ? dto.getCertNumber() : certInfo.get("certificateNumber"));
            String certName = certInfo.get("name");
            if (certName != null && !certName.isEmpty()) {
                officer.setOfficerName(certName);
            }
            officer.setTrainingProject(certInfo.get("trainingItem"));
            String validPeriod = certInfo.get("validPeriod");
            if (validPeriod != null && !validPeriod.isEmpty()) {
                officer.setValidPeriod(validPeriod);
            }
        }
        
        if (existingOfficer != null) {
            officer.setId(existingOfficer.getId());
            safetyOfficerMapper.updateById(officer);
        } else {
            safetyOfficerMapper.insert(officer);
        }
        
        log.info("安全员资格信息已保存，用户ID：{}", dto.getUserId());
    }
    
    private void saveRepairmanCert(CertVerifyDTO dto, User user, String repairCertUrl, Map<String, String> certInfo) {
        Repairman existingRepairman = repairmanMapper.selectByUserId(String.valueOf(dto.getUserId()));
        
        Repairman repairman = new Repairman();
        repairman.setUserId(dto.getUserId());
        repairman.setRepairmanName(user.getRealName());
        repairman.setIdCard(user.getIdCard());
        repairman.setIdCardFrontUrl(existingRepairman != null ? existingRepairman.getIdCardFrontUrl() : null);
        repairman.setIdCardBackUrl(existingRepairman != null ? existingRepairman.getIdCardBackUrl() : null);
        repairman.setRepairCertUrl(repairCertUrl);
        repairman.setStatus(UserStatusEnum.ENABLED.getValue());
        
        if (certInfo != null) {
            repairman.setCertNumber(dto.getCertNumber() != null ? dto.getCertNumber() : certInfo.get("certificateNumber"));
            String certName = certInfo.get("name");
            if (certName != null && !certName.isEmpty()) {
                repairman.setRepairmanName(certName);
            }
            repairman.setRepairLevel(certInfo.get("level"));
            repairman.setRepairType(certInfo.get("category"));
            String validUntil = certInfo.get("validUntil");
            if (validUntil != null && !validUntil.isEmpty()) {
                repairman.setValidUntil(parseDate(validUntil));
            }
            String issueDate = certInfo.get("issueDate");
            if (issueDate != null && !issueDate.isEmpty()) {
                repairman.setIssueDate(parseDate(issueDate));
            }
        }
        
        if (existingRepairman != null) {
            repairman.setId(existingRepairman.getId());
            repairmanMapper.updateById(repairman);
        } else {
            repairmanMapper.insert(repairman);
        }
        
        log.info("维修员资格信息已保存，用户ID：{}", dto.getUserId());
    }
    
    private void assignRoleToUser(Long userId, String roleCode) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        Role role = roleMapper.selectByRoleCode(roleCode);
        if (role == null) {
            role = createDefaultRole(roleCode);
        }
        
        if (user.getRoleId() != null) {
            Role currentRole = roleMapper.selectById(user.getRoleId());
            
            if (currentRole != null && !currentRole.getId().equals(role.getId())) {
                createRoleChangeApply(user, currentRole, role);
            }
            return;
        }
        
        user.setRoleId(role.getId());
        userMapper.updateById(user);
        
        log.info("自动分配角色成功，用户ID：{}，角色：{}", userId, roleCode);
    }
    
    private void createRoleChangeApply(User user, Role currentRole, Role applyRole) {
        RoleChangeApply apply = new RoleChangeApply();
        apply.setUserId(user.getId());
        apply.setUsername(user.getUsername());
        apply.setCurrentRoleId(currentRole.getId());
        apply.setCurrentRoleCode(currentRole.getRoleCode());
        apply.setCurrentRoleName(currentRole.getRoleName());
        apply.setApplyRoleId(applyRole.getId());
        apply.setApplyRoleCode(applyRole.getRoleCode());
        apply.setApplyRoleName(applyRole.getRoleName());
        apply.setApplyReason("用户尝试认证新角色，需要管理员审核");
        
        roleChangeApplyService.createRoleChangeApply(apply);
        log.info("角色变更申请已创建，用户ID：{}，当前角色：{}，申请角色：{}", 
                user.getId(), currentRole.getRoleName(), applyRole.getRoleName());
    }
    
    private Role createDefaultRole(String roleCode) {
        Role role = new Role();
        role.setRoleCode(roleCode);
        
        switch (roleCode) {
            case ROLE_DRIVER:
                role.setRoleName("司机");
                role.setDescription("司机角色，负责车辆驾驶");
                break;
            case ROLE_SAFETY_OFFICER:
                role.setRoleName("安全员");
                role.setDescription("安全员角色，负责安全监督");
                break;
            case ROLE_REPAIRMAN:
                role.setRoleName("维修员");
                role.setDescription("维修员角色，负责车辆维修");
                break;
            default:
                role.setRoleName("未知角色");
                role.setDescription("未知角色");
        }
        
        roleMapper.insert(role);
        
        log.info("创建默认角色成功：{}", roleCode);
        return role;
    }
    
    private String uploadImage(String base64Image, String folder) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new UserException(UserResultCode.PARAM_ERROR, "图片不能为空");
        }
        
        try {
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            
            String fileName = folder + "/" + System.currentTimeMillis() + ".jpg";
            return storageService.upload(inputStream, fileName, "image/jpeg");
            
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new UserException(UserResultCode.QUALIFICATION_VERIFY_FAILED, "图片上传失败");
        }
    }
    
    private String uploadOptionalImage(String base64Image, String folder) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }
        return uploadImage(base64Image, folder);
    }
}
