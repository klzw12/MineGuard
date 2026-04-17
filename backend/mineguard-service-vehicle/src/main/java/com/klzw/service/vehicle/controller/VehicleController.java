package com.klzw.service.vehicle.controller;

import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.result.Result;
import com.klzw.service.vehicle.dto.BestVehicleQueryDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.service.VehicleService;
import com.klzw.service.vehicle.vo.BestVehicleVO;
import com.klzw.service.vehicle.vo.VehicleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "车辆管理", description = "车辆管理接口")
@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    @Operation(summary = "创建车辆")
    @PostMapping
    public Result<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        return Result.success(createdVehicle);
    }
    
    @Operation(summary = "创建车辆并上传照片")
    @PostMapping("/create-with-photos")
    public Result<Vehicle> createVehicleWithPhotos(
            @RequestParam("vehicleNo") String vehicleNo,
            @RequestParam(value = "vehicleType", required = false, defaultValue = "1") Integer vehicleType,
            @RequestParam(value = "vehiclePhoto", required = false) MultipartFile vehiclePhoto,
            @RequestParam(value = "licensePhoto", required = false) MultipartFile licensePhoto) {
        Vehicle vehicle = vehicleService.createVehicleWithPhotos(vehicleNo, vehicleType, vehiclePhoto, licensePhoto);
        return Result.success(vehicle);
    }
    
    @Operation(summary = "更新车辆")
    @PutMapping("/{id}")
    public Result<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicle);
        return Result.success(updatedVehicle);
    }
    
    @Operation(summary = "删除车辆")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteVehicle(@PathVariable Long id) {
        boolean result = vehicleService.deleteVehicle(id);
        return Result.success(result);
    }
    
    @Operation(summary = "获取车辆详情")
    @GetMapping("/{id}")
    public Result<VehicleVO> getVehicleById(@PathVariable Long id) {
        VehicleVO vehicleVO = vehicleService.getVehicleById(id);
        return Result.success(vehicleVO);
    }
    
    @Operation(summary = "分页查询车辆")
    @GetMapping("/page")
    public Result<List<VehicleVO>> getVehiclePage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String vehicleNo,
            @RequestParam(required = false) Integer status) {
        List<VehicleVO> vehicles = vehicleService.getVehiclePage(pageNum, pageSize, vehicleNo, status);
        return Result.success(vehicles);
    }
    
    @Operation(summary = "上传车辆照片")
    @PostMapping("/{id}/photo")
    public Result<String> uploadVehiclePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String photoUrl = vehicleService.uploadVehiclePhoto(id, file);
        return Result.success(photoUrl);
    }
    
    @Operation(summary = "上传行驶证并进行OCR识别")
    @PostMapping("/{id}/license")
    public Result<Vehicle> uploadLicenseAndOCR(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Vehicle vehicle = vehicleService.uploadLicenseAndOCR(id, file);
        return Result.success(vehicle);
    }
    
    @Operation(summary = "上传行驶证正面并进行OCR识别")
    @PostMapping("/{id}/license/front")
    public Result<Vehicle> uploadLicenseFrontAndOCR(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Vehicle vehicle = vehicleService.uploadLicenseFrontAndOCR(id, file);
        return Result.success(vehicle);
    }
    
    @Operation(summary = "上传行驶证反面")
    @PostMapping("/{id}/license/back")
    public Result<Vehicle> uploadLicenseBack(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Vehicle vehicle = vehicleService.uploadLicenseBack(id, file);
        return Result.success(vehicle);
    }
    
    @Operation(summary = "上传车辆保险信息")
    @PostMapping("/{id}/insurance")
    public Result<Vehicle> uploadInsuranceInfo(@PathVariable Long id, @RequestParam String insuranceCompany, 
                                             @RequestParam String policyNo, @RequestParam String startDate, 
                                             @RequestParam String endDate) {
        Vehicle vehicle = vehicleService.uploadInsuranceInfo(id, insuranceCompany, policyNo, startDate, endDate);
        return Result.success(vehicle);
    }
    
    @Operation(summary = "更新车辆维修状态")
    @PutMapping("/{id}/maintenance")
    public Result<Vehicle> updateMaintenanceStatus(@PathVariable Long id, @RequestParam Integer maintenanceStatus) {
        Vehicle vehicle = vehicleService.updateMaintenanceStatus(id, maintenanceStatus);
        return Result.success(vehicle);
    }
    
    @Operation(summary = "选择最佳车辆", description = "根据货物重量、车辆状态、油量等因素综合评估推荐最佳车辆")
    @PostMapping("/best")
    public Result<List<VehicleInfo>> selectBestVehicles(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) BigDecimal startLongitude,
            @RequestParam(required = false) BigDecimal startLatitude,
            @RequestParam(required = false) BigDecimal cargoWeight,
            @RequestParam(required = false) String scheduledTime) {
        BestVehicleQueryDTO query = new BestVehicleQueryDTO();
        query.setDriverId(driverId);
        query.setStartLongitude(startLongitude != null ? startLongitude.doubleValue() : null);
        query.setStartLatitude(startLatitude != null ? startLatitude.doubleValue() : null);
        query.setCargoWeight(cargoWeight);
        query.setScheduledTime(scheduledTime);
        List<BestVehicleVO> vehicles = vehicleService.selectBestVehicles(query);
        List<VehicleInfo> result = vehicles.stream().map(v -> {
            VehicleInfo info = new VehicleInfo();
            info.setId(v.getId());
            info.setVehicleNo(v.getVehicleNo());
            info.setVehicleType(v.getVehicleType());
            info.setBrand(v.getBrand());
            info.setModel(v.getModel());
            info.setRatedLoad(v.getRatedLoad());
            info.setFuelLevel(v.getFuelLevel());
            info.setScore(v.getScore());
            info.setStatus(v.getStatus());
            info.setCurrentDriverId(v.getCurrentDriverId());
            info.setScore(v.getScore());
            info.setReason(v.getReason());
            info.setCargoWeight(query.getCargoWeight());
            info.setStartLongitude(query.getStartLongitude());
            info.setStartLatitude(query.getStartLatitude());
            return info;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }

    @Operation(summary = "获取所有可用车辆", description = "获取所有空闲状态的车辆列表")
    @GetMapping("/available")
    public Result<List<VehicleInfo>> getAvailableVehicles() {
        List<VehicleVO> vehicles = vehicleService.getAvailableVehicles();
        List<VehicleInfo> result = vehicles.stream().map(v -> {
            VehicleInfo info = new VehicleInfo();
            try {
                info.setId(v.getId() != null ? Long.parseLong(v.getId()) : null);
            } catch (Exception ignored) {}
            info.setVehicleNo(v.getVehicleNo());
            info.setVehicleType(v.getVehicleType());
            info.setBrand(v.getBrand());
            info.setModel(v.getModel());
            info.setRatedLoad(v.getRatedLoad());
            info.setFuelLevel(v.getFuelLevel());
            info.setStatus(v.getStatus());
            info.setPhotoUrl(v.getPhotoUrl());
            info.setLicenseFrontUrl(v.getLicenseFrontUrl());
            info.setLicenseBackUrl(v.getLicenseBackUrl());
            info.setOwner(v.getOwner());
            return info;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }
    
    @Operation(summary = "获取故障车辆列表", description = "获取所有故障状态的车辆列表，用于维修任务分配")
    @GetMapping("/fault")
    public Result<List<VehicleInfo>> getFaultVehicles() {
        List<VehicleVO> vehicles = vehicleService.getFaultVehicles();
        List<VehicleInfo> result = vehicles.stream().map(v -> {
            VehicleInfo info = new VehicleInfo();
            try {
                info.setId(v.getId() != null ? Long.parseLong(v.getId()) : null);
            } catch (Exception ignored) {}
            info.setVehicleNo(v.getVehicleNo());
            info.setVehicleType(v.getVehicleType());
            info.setBrand(v.getBrand());
            info.setModel(v.getModel());
            info.setRatedLoad(v.getRatedLoad());
            info.setFuelLevel(v.getFuelLevel());
            info.setStatus(v.getStatus());
            info.setPhotoUrl(v.getPhotoUrl());
            return info;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }
    
    @Operation(summary = "获取维护中车辆列表", description = "获取所有维护中状态的车辆列表")
    @GetMapping("/maintenance")
    public Result<List<VehicleInfo>> getMaintenanceVehicles() {
        List<VehicleVO> vehicles = vehicleService.getMaintenanceVehicles();
        List<VehicleInfo> result = vehicles.stream().map(v -> {
            VehicleInfo info = new VehicleInfo();
            try {
                info.setId(v.getId() != null ? Long.parseLong(v.getId()) : null);
            } catch (Exception ignored) {}
            info.setVehicleNo(v.getVehicleNo());
            info.setVehicleType(v.getVehicleType());
            info.setBrand(v.getBrand());
            info.setModel(v.getModel());
            info.setRatedLoad(v.getRatedLoad());
            info.setFuelLevel(v.getFuelLevel());
            info.setStatus(v.getStatus());
            info.setPhotoUrl(v.getPhotoUrl());
            return info;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }

    @Operation(summary = "检查车辆是否存在")
    @GetMapping("/{id}/exists")
    public Result<Boolean> existsById(@PathVariable Long id) {
        boolean result = vehicleService.existsById(id);
        return Result.success(result);
    }
    
    @Operation(summary = "获取车辆总数")
    @GetMapping("/count")
    public Result<Integer> getVehicleCount() {
        int count = vehicleService.getVehicleCount();
        return Result.success(count);
    }
    
    @Operation(summary = "获取所有车辆ID列表")
    @GetMapping("/ids")
    public Result<List<Long>> getVehicleIds() {
        List<Long> ids = vehicleService.getVehicleIds();
        return Result.success(ids);
    }
    
    @Operation(summary = "报废车辆", description = "将车辆标记为报废状态（软删除）")
    @PutMapping("/{id}/scrap")
    public Result<Boolean> scrapVehicle(@PathVariable Long id) {
        boolean result = vehicleService.scrapVehicle(id);
        return Result.success(result);
    }
    
    @Operation(summary = "获取维修专用车列表", description = "获取所有空闲状态的维修专用车")
    @GetMapping("/repairman")
    public Result<List<VehicleInfo>> getRepairmanVehicles() {
        List<VehicleVO> vehicles = vehicleService.getRepairmanVehicles();
        List<VehicleInfo> result = vehicles.stream().map(v -> {
            VehicleInfo info = new VehicleInfo();
            try {
                info.setId(v.getId() != null ? Long.parseLong(v.getId()) : null);
            } catch (Exception ignored) {}
            info.setVehicleNo(v.getVehicleNo());
            info.setVehicleType(v.getVehicleType());
            info.setBrand(v.getBrand());
            info.setModel(v.getModel());
            info.setRatedLoad(v.getRatedLoad());
            info.setFuelLevel(v.getFuelLevel());
            info.setStatus(v.getStatus());
            info.setPhotoUrl(v.getPhotoUrl());
            return info;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }
    
    @Operation(summary = "获取救援专用车列表", description = "获取所有空闲状态的救援专用车(安全员用)")
    @GetMapping("/safety-officer")
    public Result<List<VehicleInfo>> getSafetyOfficerVehicles() {
        List<VehicleVO> vehicles = vehicleService.getSafetyOfficerVehicles();
        List<VehicleInfo> result = vehicles.stream().map(v -> {
            VehicleInfo info = new VehicleInfo();
            try {
                info.setId(v.getId() != null ? Long.parseLong(v.getId()) : null);
            } catch (Exception ignored) {}
            info.setVehicleNo(v.getVehicleNo());
            info.setVehicleType(v.getVehicleType());
            info.setBrand(v.getBrand());
            info.setModel(v.getModel());
            info.setRatedLoad(v.getRatedLoad());
            info.setFuelLevel(v.getFuelLevel());
            info.setStatus(v.getStatus());
            info.setPhotoUrl(v.getPhotoUrl());
            return info;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }
    
    @Operation(summary = "根据用户类型获取可绑定车辆列表", description = "根据用户角色返回对应的可绑定车辆：司机返回普通车辆，维修员返回维修专用车，安全员返回救援专用车")
    @GetMapping("/bindable/{userId}")
    public Result<List<VehicleInfo>> getBindableVehicles(@PathVariable Long userId) {
        List<VehicleVO> vehicles = vehicleService.getBindableVehicles(userId);
        List<VehicleInfo> result = vehicles.stream().map(v -> {
            VehicleInfo info = new VehicleInfo();
            try {
                info.setId(v.getId() != null ? Long.parseLong(v.getId()) : null);
            } catch (Exception ignored) {}
            info.setVehicleNo(v.getVehicleNo());
            info.setVehicleType(v.getVehicleType());
            info.setBrand(v.getBrand());
            info.setModel(v.getModel());
            info.setRatedLoad(v.getRatedLoad());
            info.setFuelLevel(v.getFuelLevel());
            info.setStatus(v.getStatus());
            info.setPhotoUrl(v.getPhotoUrl());
            return info;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }
    
}
