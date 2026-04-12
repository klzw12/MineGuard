package com.klzw.service.vehicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.service.vehicle.dto.BestVehicleQueryDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.vo.BestVehicleVO;
import com.klzw.service.vehicle.vo.VehicleVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 车辆服务接口
 */
public interface VehicleService extends IService<Vehicle> {
    
    /**
     * 创建车辆
     * @param vehicle 车辆信息
     * @return 车辆信息
     */
    Vehicle createVehicle(Vehicle vehicle);
    
    /**
     * 更新车辆
     * @param id 车辆ID
     * @param vehicle 车辆信息
     * @return 车辆信息
     */
    Vehicle updateVehicle(Long id, Vehicle vehicle);
    
    /**
     * 删除车辆
     * @param id 车辆ID
     * @return 是否成功
     */
    boolean deleteVehicle(Long id);
    
    /**
     * 获取车辆详情
     * @param id 车辆ID
     * @return 车辆详情
     */
    VehicleVO getVehicleById(Long id);
    
    /**
     * 分页查询车辆
     * @param page 页码
     * @param size 每页大小
     * @param vehicleNo 车牌号
     * @param status 状态
     * @return 车辆列表
     */
    List<VehicleVO> getVehiclePage(int page, int size, String vehicleNo, Integer status);
    
    /**
     * 上传车辆照片
     * @param id 车辆ID
     * @param file 照片文件
     * @return 照片URL
     */
    String uploadVehiclePhoto(Long id, MultipartFile file);
    
    /**
     * 上传行驶证并进行OCR识别（默认正面）
     * @param id 车辆ID
     * @param file 行驶证照片
     * @return 更新后的车辆信息
     */
    Vehicle uploadLicenseAndOCR(Long id, MultipartFile file);
    
    /**
     * 上传行驶证正面并进行OCR识别
     * @param id 车辆ID
     * @param file 行驶证正面照片
     * @return 更新后的车辆信息
     */
    Vehicle uploadLicenseFrontAndOCR(Long id, MultipartFile file);
    
    /**
     * 上传行驶证反面
     * @param id 车辆ID
     * @param file 行驶证反面照片
     * @return 更新后的车辆信息
     */
    Vehicle uploadLicenseBack(Long id, MultipartFile file);
    
    /**
     * 上传车辆保险信息
     * @param id 车辆ID
     * @param insuranceCompany 保险公司
     * @param policyNo 保单号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 更新后的车辆信息
     */
    Vehicle uploadInsuranceInfo(Long id, String insuranceCompany, String policyNo, String startDate, String endDate);
    
    /**
     * 更新车辆维修状态
     * @param id 车辆ID
     * @param maintenanceStatus 维修状态
     * @return 更新后的车辆信息
     */
    Vehicle updateMaintenanceStatus(Long id, Integer maintenanceStatus);
    
    /**
     * 上传车辆保险信息
     * @param id 车辆ID
     * @param insurance 保险信息
     * @return 保险信息
     */
    com.klzw.service.vehicle.entity.VehicleInsurance uploadInsuranceInfo(Long id, com.klzw.service.vehicle.entity.VehicleInsurance insurance);
    
    /**
     * 选择最佳车辆
     * 根据货物重量、车辆状态、油量等因素综合评估推荐最佳车辆
     * @param query 查询条件
     * @return 推荐的最佳车辆列表（按得分降序）
     */
    List<BestVehicleVO> selectBestVehicles(BestVehicleQueryDTO query);
    
    /**
     * 获取所有可用车辆（空闲状态）
     * @return 可用车辆列表
     */
    List<VehicleVO> getAvailableVehicles();
    
    /**
     * 创建车辆并上传照片
     * @param vehicleNo 车牌号
     * @param vehicleType 车辆类型
     * @param vehiclePhoto 车辆照片
     * @param licensePhoto 行驶证照片
     * @return 车辆信息
     */
    Vehicle createVehicleWithPhotos(String vehicleNo, Integer vehicleType, MultipartFile vehiclePhoto, MultipartFile licensePhoto);

    /**
     * 报废车辆（软删除）
     * @param id 车辆ID
     * @return 是否成功
     */
    boolean scrapVehicle(Long id);

    /**
     * 更新车辆状态
     * @param vehicleId 车辆ID
     * @param status 状态
     */
    void updateVehicleStatus(Long vehicleId, Integer status);

    /**
     * 检查车辆是否存在
     * @param id 车辆ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 获取故障车辆列表
     * @return 故障车辆列表
     */
    List<VehicleVO> getFaultVehicles();

    /**
     * 获取维护中车辆列表
     * @return 维护中车辆列表
     */
    List<VehicleVO> getMaintenanceVehicles();

    /**
     * 获取指定类型的可用车辆
     * @param vehicleType 车辆类型
     * @return 可用车辆列表
     */
    List<VehicleVO> getAvailableVehiclesByType(Integer vehicleType);

    /**
     * 获取维修专用车列表
     * @return 维修专用车列表
     */
    List<VehicleVO> getRepairmanVehicles();

    /**
     * 获取救援专用车列表(安全员用)
     * @return 救援专用车列表
     */
    List<VehicleVO> getSafetyOfficerVehicles();

    /**
     * 根据用户类型获取可绑定的车辆列表
     * @param userId 用户ID
     * @return 可绑定的车辆列表
     */
    List<VehicleVO> getBindableVehicles(Long userId);

} 
