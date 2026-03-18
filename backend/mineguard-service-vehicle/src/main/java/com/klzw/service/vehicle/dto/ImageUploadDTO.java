package com.klzw.service.vehicle.dto;

import lombok.Data;

/**
 * 图片上传DTO
 */
@Data
public class ImageUploadDTO {
    
    private String fileType; // 图片类型：vehicle_photo, license_front, license_back, insurance
    private String fileName;
    private byte[] fileData;
    private String fileUrl; // 上传后返回的URL
    
}