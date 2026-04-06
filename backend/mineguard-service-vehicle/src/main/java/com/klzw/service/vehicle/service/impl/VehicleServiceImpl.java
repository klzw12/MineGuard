package com.klzw.service.vehicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.file.service.OcrService;
import com.klzw.common.file.service.StorageService;
import com.klzw.service.vehicle.dto.BestVehicleQueryDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.entity.VehicleStatus;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private StorageService storageService;
    
    @Resource
    private com.klzw.service.vehicle.service.VehicleInsuranceService vehicleInsuranceService;
    
    @Resource
    private com.klzw.service.vehicle.service.VehicleStatusService vehicleStatusService;
    
    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        log.info("创建车辆: {}", vehicle);
        vehicle.setStatus(VehicleStatusEnum.IDLE.getCode());
        save(vehicle);
        
        VehicleStatus vehicleStatus = new VehicleStatus();
        vehicleStatus.setVehicleId(vehicle.getId());
        vehicleStatus.setStatus(VehicleStatusEnum.IDLE.getCode());
        vehicleStatusService.save(vehicleStatus);
        
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
    public String uploadVehiclePhoto(Long id, MultipartFile file) {
        log.info("上传车辆照片: vehicleId={}, fileName={}", id, file.getOriginalFilename());
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
        }
        
        try {
            String fileName = "vehicle-photo/" + id + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String photoUrl = storageService.upload(file.getInputStream(), fileName, file.getContentType());
            
            vehicle.setPhotoUrl(photoUrl);
            updateById(vehicle);
            
            log.info("车辆照片上传成功: {}", photoUrl);
            return photoUrl;
        } catch (IOException e) {
            log.error("上传车辆照片失败", e);
            throw new VehicleException(VehicleResultCode.OPERATION_FAILED, "上传照片失败：" + file.getOriginalFilename(), e);
        }
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
        
        try {
            byte[] fileBytes = file.getBytes();
            
            String ocrResult = ocrService.recognizeVehicleLicense(fileBytes);
            Map<String, String> parseResult = ocrService.parseVehicleLicenseFront(ocrResult);
            
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
                    log.warn("解析注册日期失败：{}", parseResult.get("registerDate"));
                }
            }
            if (parseResult.containsKey("issueDate")) {
                try {
                    vehicle.setIssueDate(LocalDate.parse(parseResult.get("issueDate")));
                } catch (Exception e) {
                    log.warn("解析发证日期失败：{}", parseResult.get("issueDate"));
                }
            }
            
            String fileName = "vehicle-license/" + id + "/front_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String licenseFrontUrl = storageService.upload(new ByteArrayInputStream(fileBytes), fileName, file.getContentType());
            vehicle.setLicenseFrontUrl(licenseFrontUrl);
            
            log.info("行驶证正面上传成功: {}", licenseFrontUrl);
        } catch (Exception e) {
            log.error("行驶证OCR识别失败", e);
            throw new VehicleException(VehicleResultCode.OPERATION_FAILED, "OCR识别失败：" + e.getMessage(), e);
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
        
        try {
            byte[] fileBytes = file.getBytes();
            
            String fileName = "vehicle-license/" + id + "/back_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String licenseBackUrl = storageService.upload(new ByteArrayInputStream(fileBytes), fileName, file.getContentType());
            vehicle.setLicenseBackUrl(licenseBackUrl);
            
            log.info("行驶证反面上传成功: {}", licenseBackUrl);
            
            String ocrResult = ocrService.recognizeVehicleLicenseBack(file);
            Map<String, String> parseResult = ocrService.parseVehicleLicenseBack(ocrResult);
            
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
        
        com.klzw.service.vehicle.dto.VehicleInsuranceDTO insuranceDTO = new com.klzw.service.vehicle.dto.VehicleInsuranceDTO();
        insuranceDTO.setVehicleId(id);
        insuranceDTO.setInsuranceCompany(insuranceCompany);
        insuranceDTO.setInsuranceNumber(policyNo);
        insuranceDTO.setInsuranceType(InsuranceTypeEnum.COMPULSORY.getCode());
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
        log.info("选择最佳车辆: cargoWeight={}, vehicleType={}, scheduledTime={}", query.getCargoWeight(), query.getVehicleType(), query.getScheduledTime());

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Vehicle> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        wrapper.eq(Vehicle::getStatus, VehicleStatusEnum.IDLE.getCode());

        if (query.getVehicleType() != null) {
            wrapper.eq(Vehicle::getVehicleType, query.getVehicleType());
        }

        if (query.getExcludeVehicleIds() != null && !query.getExcludeVehicleIds().isEmpty()) {
            wrapper.notIn(Vehicle::getId, query.getExcludeVehicleIds());
        }

        if (query.getScheduledTime() != null && !query.getScheduledTime().isEmpty()) {
            try {
                java.time.LocalDate targetDate = java.time.LocalDate.parse(query.getScheduledTime());
                java.time.LocalDateTime startTime = targetDate.atStartOfDay();
                java.time.LocalDateTime endTime = targetDate.atTime(23, 59, 59);
                List<Long> busyVehicleIds = getBaseMapper().findBusyVehicleIds(startTime, endTime);
                if (busyVehicleIds != null && !busyVehicleIds.isEmpty()) {
                    log.info("时间段 {} 已有任务的车辆ID: {}", query.getScheduledTime(), busyVehicleIds);
                    if (!busyVehicleIds.isEmpty()) {
                        wrapper.notIn(Vehicle::getId, busyVehicleIds);
                    }
                }
            } catch (Exception e) {
                log.warn("解析计划时间失败: {}, 将不过滤车辆", query.getScheduledTime());
            }
        }

        wrapper.orderByDesc(Vehicle::getFuelLevel);

        List<Vehicle> vehicles = list(wrapper);

        List<BestVehicleVO> result = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            BestVehicleVO vo = new BestVehicleVO();
            vo.setId(vehicle.getId());
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
    
    @Override
    public Vehicle createVehicleWithPhotos(String vehicleNo, Integer vehicleType, MultipartFile vehiclePhoto, MultipartFile licensePhoto) {
        log.info("创建车辆并上传照片: vehicleNo={}, vehicleType={}", vehicleNo, vehicleType);
        
        if (licensePhoto != null && !licensePhoto.isEmpty()) {
            try {
                byte[] fileBytes = licensePhoto.getBytes();
                String ocrResult = ocrService.recognizeVehicleLicense(fileBytes);
                Map<String, String> parseResult = ocrService.parseVehicleLicenseFront(ocrResult);
                
                String ocrPlateNumber = parseResult.get("plateNumber");
                if (ocrPlateNumber != null && !ocrPlateNumber.isEmpty()) {
                    if (!vehicleNo.equals(ocrPlateNumber)) {
                        log.error("车牌号不一致: 传入={}, OCR识别={}", vehicleNo, ocrPlateNumber);
                        throw new VehicleException(VehicleResultCode.VEHICLE_NO_MISMATCH, 
                            "车牌号与行驶证识别结果不一致，传入：" + vehicleNo + "，识别：" + ocrPlateNumber);
                    }
                    log.info("车牌号验证通过: {}", vehicleNo);
                }
            } catch (VehicleException e) {
                throw e;
            } catch (Exception e) {
                log.error("行驶证OCR识别失败", e);
                throw new VehicleException(VehicleResultCode.OPERATION_FAILED, "行驶证OCR识别失败：" + e.getMessage(), e);
            }
        }
        
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNo(vehicleNo);
        vehicle.setVehicleType(vehicleType != null ? vehicleType : 1);
        vehicle.setStatus(VehicleStatusEnum.IDLE.getCode());
        
        save(vehicle);
        
        if (vehiclePhoto != null && !vehiclePhoto.isEmpty()) {
            try {
                uploadVehiclePhoto(vehicle.getId(), vehiclePhoto);
            } catch (Exception e) {
                log.error("上传车辆照片失败", e);
            }
        }
        
        if (licensePhoto != null && !licensePhoto.isEmpty()) {
            try {
                uploadLicenseFrontAndOCR(vehicle.getId(), licensePhoto);
            } catch (Exception e) {
                log.error("上传行驶证照片失败", e);
            }
        }
        
        return getById(vehicle.getId());
    }
    
    @Override
    public boolean scrapVehicle(Long id) {
        log.info("报废车辆: id={}", id);
        Vehicle vehicle = getById(id);
        if (vehicle == null) {
            throw new VehicleException(VehicleResultCode.VEHICLE_NOT_FOUND, "车辆不存在：" + id);
        }
        
        vehicle.setDeleted(1);
        updateById(vehicle);
        
        VehicleStatus vehicleStatus = vehicleStatusService.getByVehicleId(id);
        if (vehicleStatus != null) {
            vehicleStatus.setStatus(VehicleStatusEnum.SCRAPPED.getCode());
            vehicleStatusService.updateById(vehicleStatus);
        }
        
        return true;
    }
    
    @Override
    public void updateVehicleStatus(Long vehicleId, Integer status) {
        log.info("更新车辆状态: vehicleId={}, status={}", vehicleId, status);
        
        Vehicle vehicle = getById(vehicleId);
        if (vehicle != null) {
            vehicle.setStatus(status);
            updateById(vehicle);
        }
        
        VehicleStatus vehicleStatus = vehicleStatusService.getByVehicleId(vehicleId);
        if (vehicleStatus != null) {
            vehicleStatus.setStatus(status);
            vehicleStatusService.updateById(vehicleStatus);
        }
    }
    
    @Override
    public boolean existsById(Long id) {
        log.info("检查车辆是否存在: id={}", id);
        return getById(id) != null;
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
    
    private VehicleVO convertToVO(Vehicle vehicle) {
        VehicleVO vo = new VehicleVO();
        vo.setId(vehicle.getId() != null ? vehicle.getId().toString() : null);
        vo.setVehicleNo(vehicle.getVehicleNo());
        vo.setVehicleType(vehicle.getVehicleType());
        vo.setBrand(vehicle.getBrand());
        vo.setModel(vehicle.getModel());
        vo.setStatus(vehicle.getStatus());
        vo.setFuelLevel(vehicle.getFuelLevel());
        vo.setPhotoUrl(getPresignedUrl(vehicle.getPhotoUrl()));
        vo.setLicenseFrontUrl(getPresignedUrl(vehicle.getLicenseFrontUrl()));
        vo.setLicenseBackUrl(getPresignedUrl(vehicle.getLicenseBackUrl()));
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
        
        try {
            List<com.klzw.service.vehicle.entity.VehicleInsurance> insurances = vehicleInsuranceService.getVehicleInsurance(vehicle.getId());
            if (insurances != null && !insurances.isEmpty()) {
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
    
    private String getPresignedUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        try {
            return storageService.getUrl(fileUrl, 3600);
        } catch (Exception e) {
            log.warn("获取预签名URL失败: {}", fileUrl);
            return fileUrl;
        }
    }
    
}
