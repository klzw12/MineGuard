package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.file.service.OcrService;
import com.klzw.service.vehicle.dto.BestVehicleQueryDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.enums.InsuranceTypeEnum;
import com.klzw.service.vehicle.enums.VehicleStatusEnum;
import com.klzw.service.vehicle.mapper.VehicleMapper;
import com.klzw.service.vehicle.service.VehicleService;
import com.klzw.service.vehicle.exception.VehicleException;
import com.klzw.service.vehicle.exception.VehicleResultCode;
import com.klzw.service.vehicle.vo.BestVehicleVO;
import com.klzw.service.vehicle.vo.VehicleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Vehicle> pageObj = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Vehicle> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        if (vehicleNo != null && !vehicleNo.isEmpty()) {
            wrapper.like(Vehicle::getVehicleNo, vehicleNo);
        }
        if (status != null) {
            wrapper.eq(Vehicle::getStatus, status);
        }
        wrapper.orderByDesc(Vehicle::getCreateTime);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Vehicle> result = 
                getBaseMapper().selectPage(pageObj, wrapper);
        
        return result.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public boolean bindUser(Long id, Long userId) {
        log.info("绑定用户功能已废弃，车辆通过调度动态分配: vehicleId={}, userId={}", id, userId);
        return true;
    }
    
    @Override
    public boolean unbindUser(Long id) {
        log.info("解绑用户功能已废弃，车辆通过调度动态分配: vehicleId={}", id);
        return true;
    }
    
    @Override
    public String uploadVehiclePhoto(Long id, MultipartFile file) {
        log.info("上传车辆照片: vehicleId={}, fileName={}", id, file.getOriginalFilename());
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
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
            throw new VehicleException(VehicleResultCode.OPERATION_FAILED, "上传照片失败：" + file.getOriginalFilename(), e);
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
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
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
            throw new VehicleException(VehicleResultCode.OPERATION_FAILED, "上传行驶证正面失败：" + file.getOriginalFilename(), e);
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
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
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
            throw new VehicleException(VehicleResultCode.OPERATION_FAILED, "上传行驶证反面失败：" + file.getOriginalFilename(), e);
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
                vehicle.setRemark(parseResult.get("remarks"));
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
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
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
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
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
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
        }
        
        vehicle.setStatus(maintenanceStatus);
        updateById(vehicle);
        
        return vehicle;
    }
    
    @Override
    public List<BestVehicleVO> selectBestVehicles(com.klzw.service.vehicle.dto.BestVehicleQueryDTO query) {
        log.info("选择最佳车辆: cargoWeight={}, vehicleType={}", query.getCargoWeight(), query.getVehicleType());
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Vehicle> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        wrapper.eq(Vehicle::getStatus, VehicleStatusEnum.IDLE.getCode());
        
        if (query.getVehicleType() != null) {
            wrapper.eq(Vehicle::getVehicleType, query.getVehicleType());
        }
        
        if (query.getExcludeVehicleIds() != null && !query.getExcludeVehicleIds().isEmpty()) {
            wrapper.notIn(Vehicle::getId, query.getExcludeVehicleIds());
        }
        
        wrapper.orderByDesc(Vehicle::getFuelLevel);
        
        List<Vehicle> vehicles = list(wrapper);
        
        List<BestVehicleVO> result = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            BestVehicleVO vo = new BestVehicleVO();
            vo.setVehicleId(vehicle.getId());
            vo.setVehicleNo(vehicle.getVehicleNo());
            vo.setVehicleType(vehicle.getVehicleType());
            vo.setBrand(vehicle.getBrand());
            vo.setModel(vehicle.getModel());
            vo.setRatedLoad(vehicle.getRatedLoad());
            vo.setFuelLevel(vehicle.getFuelLevel());
            vo.setStatus(vehicle.getStatus());
            
            int score = calculateVehicleScore(vehicle, query);
            vo.setScore(score);
            vo.setReason(generateRecommendReason(vehicle, score));
            
            result.add(vo);
        }
        
        result.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        
        return result.stream().limit(5).collect(Collectors.toList());
    }
    
    @Override
    public List<VehicleVO> getAvailableVehicles() {
        log.info("获取所有可用车辆");
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Vehicle> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getStatus, VehicleStatusEnum.IDLE.getCode());
        wrapper.orderByDesc(Vehicle::getFuelLevel);
        
        List<Vehicle> vehicles = list(wrapper);
        return vehicles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    private int calculateVehicleScore(Vehicle vehicle, com.klzw.service.vehicle.dto.BestVehicleQueryDTO query) {
        int score = 0;
        
        if (vehicle.getFuelLevel() != null) {
            score += vehicle.getFuelLevel();
        }
        
        if (query.getCargoWeight() != null && vehicle.getRatedLoad() != null) {
            try {
                String ratedLoadStr = vehicle.getRatedLoad();
                if (ratedLoadStr.contains("kg") || ratedLoadStr.contains("KG")) {
                    ratedLoadStr = ratedLoadStr.replaceAll("[^0-9.]", "");
                    double ratedLoadKg = Double.parseDouble(ratedLoadStr);
                    double ratedLoadTon = ratedLoadKg / 1000;
                    if (ratedLoadTon >= query.getCargoWeight().doubleValue()) {
                        score += 30;
                        double loadRate = query.getCargoWeight().doubleValue() / ratedLoadTon;
                        if (loadRate >= 0.7 && loadRate <= 0.9) {
                            score += 20;
                        }
                    } else {
                        score -= 50;
                    }
                }
            } catch (Exception e) {
                log.warn("解析额定载重失败: {}", vehicle.getRatedLoad());
            }
        }
        
        return Math.max(0, score);
    }
    
    private String generateRecommendReason(Vehicle vehicle, int score) {
        StringBuilder reason = new StringBuilder();
        
        if (vehicle.getFuelLevel() != null) {
            if (vehicle.getFuelLevel() >= 70) {
                reason.append("油量充足(").append(vehicle.getFuelLevel()).append("%); ");
            } else if (vehicle.getFuelLevel() >= 40) {
                reason.append("油量适中(").append(vehicle.getFuelLevel()).append("%); ");
            } else {
                reason.append("油量较低(").append(vehicle.getFuelLevel()).append("%); ");
            }
        }
        
        if (vehicle.getRatedLoad() != null) {
            reason.append("额定载重: ").append(vehicle.getRatedLoad());
        }
        
        return reason.toString();
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
        vo.setStatus(vehicle.getStatus());
        vo.setFuelLevel(vehicle.getFuelLevel());
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
        vo.setRemarks(vehicle.getRemark());
        vo.setInspectionRecord(vehicle.getInspectionRecord());
        
        // 查询保险信息并填充
        try {
            List<com.klzw.service.vehicle.entity.VehicleInsurance> insurances = vehicleInsuranceService.getVehicleInsurance(vehicle.getId());
            if (insurances != null && !insurances.isEmpty()) {
                // 获取当前有效的保险（状态为 1 且未过期）
                com.klzw.service.vehicle.entity.VehicleInsurance currentInsurance = insurances.stream()
                    .filter(ins -> ins.getStatus() == 1 && 
                                   !ins.getExpiryDate().isBefore(java.time.LocalDate.now()))
                    .findFirst()
                    .orElse(insurances.get(0));
                
                vo.setInsuranceNo(currentInsurance.getInsuranceNumber());
                vo.setInsuranceCompany(currentInsurance.getInsuranceCompany());
                vo.setInsuranceStartDate(currentInsurance.getStartDate());
                vo.setInsuranceEndDate(currentInsurance.getExpiryDate());
                vo.setInsuranceStatus(currentInsurance.getStatus().toString());
            }
        } catch (Exception e) {
            log.warn("查询车辆保险信息失败：vehicleId={}", vehicle.getId());
        }
        
        vo.setCreateTime(vehicle.getCreateTime());
        vo.setUpdateTime(vehicle.getUpdateTime());
        return vo;
    }
    
}
