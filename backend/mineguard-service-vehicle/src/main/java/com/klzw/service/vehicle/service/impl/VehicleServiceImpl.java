package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.file.service.OcrService;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.enums.InsuranceTypeEnum;
import com.klzw.service.vehicle.enums.VehicleStatusEnum;
import com.klzw.service.vehicle.mapper.VehicleMapper;
import com.klzw.service.vehicle.service.VehicleService;
import com.klzw.service.vehicle.vo.VehicleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 车辆服务实现类
 */
@Slf4j
@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {
    
    @Resource
    private OcrService ocrService;
    
    @Resource
    private com.klzw.service.vehicle.service.VehicleInsuranceService vehicleInsuranceService;
    
    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        log.info("创建车辆: {}", vehicle);
        save(vehicle);
        return vehicle;
    }
    
    @Override
    public Vehicle updateVehicle(Long id, Vehicle vehicle) {
        log.info("更新车辆: id={}, vehicle={}", id, vehicle);
        vehicle.setId(id);
        updateById(vehicle);
        return vehicle;
    }
    
    @Override
    public boolean deleteVehicle(Long id) {
        log.info("删除车辆: id={}", id);
        return removeById(id);
    }
    
    @Override
    public VehicleVO getVehicleById(Long id) {
        log.info("获取车辆详情: id={}", id);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            return null;
        }
        return convertToVO(vehicle);
    }
    
    @Override
    public List<VehicleVO> getVehiclePage(int page, int size, String vehicleNo, Integer status) {
        log.info("分页查询车辆: page={}, size={}, vehicleNo={}, status={}", page, size, vehicleNo, status);
        // TODO: 实现分页查询逻辑
        return null;
    }
    
    @Override
    public boolean bindUser(Long id, Long userId) {
        log.info("绑定用户: vehicleId={}, userId={}", id, userId);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            return false;
        }
        vehicle.setUserId(userId);
        return updateById(vehicle);
    }
    
    @Override
    public boolean unbindUser(Long id) {
        log.info("解绑用户: vehicleId={}", id);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            return false;
        }
        vehicle.setUserId(null);
        return updateById(vehicle);
    }
    
    @Override
    public String uploadVehiclePhoto(Long id, MultipartFile file) {
        log.info("上传车辆照片: vehicleId={}, fileName={}", id, file.getOriginalFilename());
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = "./uploads/vehicle/" + fileName;
        
        // 保存文件
        try {
            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("上传车辆照片失败", e);
            throw new RuntimeException("上传照片失败");
        }
        
        // 更新车辆照片URL
        String photoUrl = "/uploads/vehicle/" + fileName;
        vehicle.setPhotoUrl(photoUrl);
        updateById(vehicle);
        
        return photoUrl;
    }
    
    @Override
    public Vehicle uploadLicenseAndOCR(Long id, MultipartFile file) {
        log.info("上传行驶证正面并进行OCR识别: vehicleId={}", id);
        return uploadLicenseFrontAndOCR(id, file);
    }
    
    @Override
    public Vehicle uploadLicenseFrontAndOCR(Long id, MultipartFile file) {
        log.info("上传行驶证正面并进行OCR识别: vehicleId={}", id);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        
        // 上传行驶证正面文件
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = "./uploads/vehicle/license/front/" + fileName;
        
        try {
            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("上传行驶证正面失败", e);
            throw new RuntimeException("上传行驶证正面失败");
        }
        
        // 更新行驶证正面URL
        String licenseFrontUrl = "/uploads/vehicle/license/front/" + fileName;
        vehicle.setLicenseFrontUrl(licenseFrontUrl);
        
        // 调用OCR服务进行行驶证正面识别
        try {
            String ocrResult = ocrService.recognizeVehicleLicense(file);
            Map<String, String> parseResult = ocrService.parseVehicleLicenseFront(ocrResult);
            
            // 填充车辆信息
            if (parseResult.containsKey("plateNumber")) {
                vehicle.setVehicleNo(parseResult.get("plateNumber"));
            }
            if (parseResult.containsKey("owner")) {
                vehicle.setOwner(parseResult.get("owner"));
            }
            if (parseResult.containsKey("address")) {
                vehicle.setAddress(parseResult.get("address"));
            }
            if (parseResult.containsKey("brandModel")) {
                vehicle.setBrandModel(parseResult.get("brandModel"));
                // 从品牌型号中提取品牌和型号
                String brandModel = parseResult.get("brandModel");
                if (brandModel != null && brandModel.length() > 0) {
                    String[] parts = brandModel.split("\\s+");
                    if (parts.length > 0) {
                        vehicle.setBrand(parts[0]);
                    }
                    if (parts.length > 1) {
                        vehicle.setModel(parts[1]);
                    }
                }
            }
            if (parseResult.containsKey("vehicleModel")) {
                vehicle.setVehicleModel(parseResult.get("vehicleModel"));
            }
            if (parseResult.containsKey("engineNumber")) {
                vehicle.setEngineNumber(parseResult.get("engineNumber"));
            }
            if (parseResult.containsKey("vin")) {
                vehicle.setVin(parseResult.get("vin"));
            }
            if (parseResult.containsKey("useNature")) {
                vehicle.setUseNature(parseResult.get("useNature"));
            }
            if (parseResult.containsKey("registerDate")) {
                try {
                    vehicle.setRegisterDate(LocalDate.parse(parseResult.get("registerDate")));
                } catch (Exception e) {
                    log.warn("解析注册日期失败: {}", parseResult.get("registerDate"));
                }
            }
            if (parseResult.containsKey("issueDate")) {
                try {
                    vehicle.setIssueDate(LocalDate.parse(parseResult.get("issueDate")));
                } catch (Exception e) {
                    log.warn("解析发证日期失败: {}", parseResult.get("issueDate"));
                }
            }
        } catch (Exception e) {
            log.error("行驶证OCR识别失败", e);
        }
        
        updateById(vehicle);
        
        return vehicle;
    }
    
    @Override
    public Vehicle uploadLicenseBack(Long id, MultipartFile file) {
        log.info("上传行驶证反面: vehicleId={}", id);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        
        // 上传行驶证反面文件
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = "./uploads/vehicle/license/back/" + fileName;
        
        try {
            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("上传行驶证反面失败", e);
            throw new RuntimeException("上传行驶证反面失败");
        }
        
        // 更新行驶证反面URL
        String licenseBackUrl = "/uploads/vehicle/license/back/" + fileName;
        vehicle.setLicenseBackUrl(licenseBackUrl);
        
        // 调用OCR服务进行行驶证反面识别
        try {
            String ocrResult = ocrService.recognizeVehicleLicenseBack(file);
            Map<String, String> parseResult = ocrService.parseVehicleLicenseBack(ocrResult);
            
            // 填充车辆信息
            if (parseResult.containsKey("seatingCapacity")) {
                try {
                    vehicle.setSeatingCapacity(Integer.parseInt(parseResult.get("seatingCapacity")));
                } catch (Exception e) {
                    log.warn("解析核定载人数失败: {}", parseResult.get("seatingCapacity"));
                }
            }
            if (parseResult.containsKey("totalMass")) {
                vehicle.setTotalMass(parseResult.get("totalMass"));
            }
            if (parseResult.containsKey("curbWeight")) {
                vehicle.setCurbWeight(parseResult.get("curbWeight"));
            }
            if (parseResult.containsKey("ratedLoad")) {
                vehicle.setRatedLoad(parseResult.get("ratedLoad"));
            }
            if (parseResult.containsKey("dimensions")) {
                vehicle.setDimensions(parseResult.get("dimensions"));
            }
            if (parseResult.containsKey("remarks")) {
                vehicle.setRemarks(parseResult.get("remarks"));
            }
            if (parseResult.containsKey("inspectionRecord")) {
                vehicle.setInspectionRecord(parseResult.get("inspectionRecord"));
            }
        } catch (Exception e) {
            log.error("行驶证反面OCR识别失败", e);
        }
        
        updateById(vehicle);
        
        return vehicle;
    }
    
    @Override
    public Vehicle uploadInsuranceInfo(Long id, String insuranceCompany, String policyNo, String startDate, String endDate) {
        log.info("上传车辆保险信息: vehicleId={}, insuranceCompany={}, policyNo={}", id, insuranceCompany, policyNo);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        
        // 创建保险DTO
        com.klzw.service.vehicle.dto.VehicleInsuranceDTO insuranceDTO = new com.klzw.service.vehicle.dto.VehicleInsuranceDTO();
        insuranceDTO.setVehicleId(id);
        insuranceDTO.setInsuranceCompany(insuranceCompany);
        insuranceDTO.setInsuranceNumber(policyNo);
        insuranceDTO.setInsuranceType(InsuranceTypeEnum.COMPULSORY.getCode()); // 交强险
        insuranceDTO.setStartDate(java.time.LocalDate.parse(startDate));
        insuranceDTO.setExpiryDate(java.time.LocalDate.parse(endDate));
        
        vehicleInsuranceService.addInsurance(insuranceDTO);
        
        return vehicle;
    }
    
    @Override
    public com.klzw.service.vehicle.entity.VehicleInsurance uploadInsuranceInfo(Long id, com.klzw.service.vehicle.entity.VehicleInsurance insurance) {
        log.info("上传车辆保险信息: vehicleId={}, insuranceCompany={}", id, insurance.getInsuranceCompany());
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        
        // 转换实体类为DTO
        com.klzw.service.vehicle.dto.VehicleInsuranceDTO insuranceDTO = new com.klzw.service.vehicle.dto.VehicleInsuranceDTO();
        insuranceDTO.setVehicleId(id);
        insuranceDTO.setInsuranceCompany(insurance.getInsuranceCompany());
        insuranceDTO.setInsuranceNumber(insurance.getInsuranceNumber());
        insuranceDTO.setInsuranceType(insurance.getInsuranceType());
        insuranceDTO.setInsuranceAmount(insurance.getInsuranceAmount());
        insuranceDTO.setStartDate(insurance.getStartDate());
        insuranceDTO.setExpiryDate(insurance.getExpiryDate());
        insuranceDTO.setRemark(insurance.getRemark());
        
        return vehicleInsuranceService.addInsurance(insuranceDTO);
    }
    
    @Override
    public Vehicle updateMaintenanceStatus(Long id, Integer maintenanceStatus) {
        log.info("更新车辆维修状态: vehicleId={}, maintenanceStatus={}", id, maintenanceStatus);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        
        // 更新维修状态
        vehicle.setStatus(maintenanceStatus);
        updateById(vehicle);
        
        return vehicle;
    }
    
    /**
     * 转换为VO对象
     * @param vehicle 车辆实体
     * @return 车辆VO
     */
    private VehicleVO convertToVO(Vehicle vehicle) {
        VehicleVO vo = new VehicleVO();
        vo.setId(vehicle.getId() != null ? vehicle.getId().toString() : null);
        vo.setVehicleNo(vehicle.getVehicleNo());
        vo.setVehicleType(vehicle.getVehicleType());
        vo.setBrand(vehicle.getBrand());
        vo.setModel(vehicle.getModel());
        vo.setUserId(vehicle.getUserId() != null ? vehicle.getUserId().toString() : null);
        vo.setStatus(vehicle.getStatus());
        vo.setPhotoUrl(vehicle.getPhotoUrl());
        vo.setLicenseFrontUrl(vehicle.getLicenseFrontUrl());
        vo.setLicenseBackUrl(vehicle.getLicenseBackUrl());
        vo.setOwner(vehicle.getOwner());
        vo.setAddress(vehicle.getAddress());
        vo.setBrandModel(vehicle.getBrandModel());
        vo.setVehicleModel(vehicle.getVehicleModel());
        vo.setEngineNumber(vehicle.getEngineNumber());
        vo.setVin(vehicle.getVin());
        vo.setUseNature(vehicle.getUseNature());
        vo.setRegisterDate(vehicle.getRegisterDate());
        vo.setIssueDate(vehicle.getIssueDate());
        vo.setSeatingCapacity(vehicle.getSeatingCapacity());
        vo.setTotalMass(vehicle.getTotalMass());
        vo.setCurbWeight(vehicle.getCurbWeight());
        vo.setRatedLoad(vehicle.getRatedLoad());
        vo.setDimensions(vehicle.getDimensions());
        vo.setRemarks(vehicle.getRemarks());
        vo.setInspectionRecord(vehicle.getInspectionRecord());
        // 保险相关字段
        // 这里需要从保险表或其他地方获取保险信息
        // 暂时设置为null，实际实现时需要根据业务逻辑获取
        vo.setInsuranceNo(null);
        vo.setInsuranceCompany(null);
        vo.setInsuranceStartDate(null);
        vo.setInsuranceEndDate(null);
        vo.setInsuranceStatus(null);
        vo.setCreateTime(vehicle.getCreateTime());
        vo.setUpdateTime(vehicle.getUpdateTime());
        return vo;
    }
    
}
