package com.klzw.service.vehicle.service;

import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.service.impl.VehicleServiceImpl;
import com.klzw.service.vehicle.vo.VehicleVO;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 车辆服务测试类
 */
public class VehicleServiceTest {
    
    @Mock
    private VehicleServiceImpl vehicleService;
    
    public VehicleServiceTest() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testCreateVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNo("京A12345");
        vehicle.setBrand("特斯拉");
        vehicle.setModel("Model 3");
        
        when(vehicleService.createVehicle(vehicle)).thenReturn(vehicle);
        
        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
        assertEquals("京A12345", result.getVehicleNo());
    }
    
    @Test
    public void testGetVehicleById() {
        Long vehicleId = 1L;
        VehicleVO vehicleVO = new VehicleVO();
        vehicleVO.setId(vehicleId.toString());
        vehicleVO.setVehicleNo("京A12345");
        
        when(vehicleService.getVehicleById(vehicleId)).thenReturn(vehicleVO);
        
        VehicleVO result = vehicleService.getVehicleById(vehicleId);
        assertNotNull(result);
        assertEquals(vehicleId.toString(), result.getId());
    }
    

    
}
