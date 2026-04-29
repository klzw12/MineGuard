package com.klzw.service.vehicle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.DriverClient;
import com.klzw.common.core.domain.dto.DriverVehicleInfo;
import com.klzw.common.core.result.Result;
import com.klzw.common.file.service.OcrService;
import com.klzw.common.file.service.StorageService;
import com.klzw.service.vehicle.dto.BestVehicleQueryDTO;
import com.klzw.service.vehicle.dto.VehicleInsuranceDTO;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.entity.VehicleInsurance;
import com.klzw.service.vehicle.entity.VehicleStatus;
import com.klzw.service.vehicle.enums.InsuranceTypeEnum;
import com.klzw.service.vehicle.enums.VehicleStatusEnum;
import com.klzw.service.vehicle.exception.VehicleException;
import com.klzw.service.vehicle.exception.VehicleResultCode;
import com.klzw.service.vehicle.mapper.VehicleMapper;
import com.klzw.service.vehicle.service.impl.VehicleServiceImpl;
import com.klzw.service.vehicle.vo.BestVehicleVO;
import com.klzw.service.vehicle.vo.VehicleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceImplTest {

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @Mock
    private VehicleMapper vehicleMapper;

    @Mock
    private OcrService ocrService;

    @Mock
    private StorageService storageService;

    @Mock
    private VehicleInsuranceService vehicleInsuranceService;

    @Mock
    private VehicleStatusService vehicleStatusService;

    @Mock
    private DriverClient driverClient;

    @Mock
    private MultipartFile multipartFile;

    private Vehicle vehicle;
    private VehicleStatus vehicleStatus;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setVehicleNo("京A12345");
        vehicle.setVehicleType(1);
        vehicle.setBrand("解放");
        vehicle.setModel("J6P");
        vehicle.setStatus(VehicleStatusEnum.IDLE.getCode());
        vehicle.setFuelLevel(80);
        vehicle.setRatedLoad("10000kg");

        vehicleStatus = new VehicleStatus();
        vehicleStatus.setId(1L);
        vehicleStatus.setVehicleId(1L);
        vehicleStatus.setStatus(VehicleStatusEnum.IDLE.getCode());

        // 初始化baseMapper
        try {
            java.lang.reflect.Field baseMapperField = com.baomidou.mybatisplus.extension.repository.CrudRepository.class.getDeclaredField("baseMapper");
            baseMapperField.setAccessible(true);
            baseMapperField.set(vehicleService, vehicleMapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void createVehicle() {
        when(vehicleMapper.insert(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle savedVehicle = invocation.getArgument(0);
            savedVehicle.setId(1L);
            return 1;
        });
        when(vehicleStatusService.save(any(VehicleStatus.class))).thenReturn(true);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(VehicleStatusEnum.IDLE.getCode(), result.getStatus());
    }

    @Test
    void updateVehicle() {
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);

        Vehicle updatedVehicle = new Vehicle();
        updatedVehicle.setVehicleNo("京A67890");
        updatedVehicle.setBrand("东风");

        Vehicle result = vehicleService.updateVehicle(1L, updatedVehicle);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("京A67890", result.getVehicleNo());
        assertEquals("东风", result.getBrand());
    }

    @Test
    void deleteVehicle() {
        when(vehicleMapper.deleteById(1L)).thenReturn(1);

        boolean result = vehicleService.deleteVehicle(1L);
        assertTrue(result);
    }

    @Test
    void getVehicleById() {
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);

        VehicleVO result = vehicleService.getVehicleById(1L);
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("京A12345", result.getVehicleNo());
    }

    @Test
    void getVehiclePage() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(vehicle);

        Page<Vehicle> page = new Page<>(1, 10);
        page.setRecords(vehicles);
        page.setTotal(1L);

        when(vehicleMapper.selectPage(any(Page.class), any())).thenReturn(page);

        List<VehicleVO> result = vehicleService.getVehiclePage(1, 10, "京A", VehicleStatusEnum.IDLE.getCode());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("京A12345", result.get(0).getVehicleNo());
    }

    @Test
    void uploadVehiclePhoto() throws IOException {
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(multipartFile.getOriginalFilename()).thenReturn("vehicle.jpg");
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(storageService.upload(any(ByteArrayInputStream.class), anyString(), anyString())).thenReturn("photo-url");
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);

        String result = vehicleService.uploadVehiclePhoto(1L, multipartFile);
        assertEquals("photo-url", result);
    }

    @Test
    void uploadVehiclePhoto_VehicleNotFound() {
        when(vehicleMapper.selectById(1L)).thenReturn(null);

        VehicleException exception = assertThrows(VehicleException.class, () -> vehicleService.uploadVehiclePhoto(1L, multipartFile));
        assertEquals(VehicleResultCode.VEHICLE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void uploadLicenseFrontAndOCR() throws IOException {
        Map<String, String> parseResult = new HashMap<>();
        parseResult.put("plateNumber", "京A12345");
        parseResult.put("owner", "张三");
        parseResult.put("brandModel", "解放 J6P");
        parseResult.put("registerDate", "2020-01-01");

        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(ocrService.recognizeVehicleLicense(any(byte[].class))).thenReturn("ocr-result");
        when(ocrService.parseVehicleLicenseFront("ocr-result")).thenReturn(parseResult);
        when(storageService.upload(any(ByteArrayInputStream.class), anyString(), anyString())).thenReturn("license-url");
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);

        Vehicle result = vehicleService.uploadLicenseFrontAndOCR(1L, multipartFile);
        assertNotNull(result);
        assertEquals("京A12345", result.getVehicleNo());
        assertEquals("张三", result.getOwner());
        assertEquals("解放", result.getBrand());
        assertEquals("J6P", result.getModel());
    }

    @Test
    void uploadLicenseBack() throws IOException {
        Map<String, String> parseResult = new HashMap<>();
        parseResult.put("seatingCapacity", "2");
        parseResult.put("totalMass", "18000kg");
        parseResult.put("ratedLoad", "10000kg");

        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(ocrService.recognizeVehicleLicenseBack(any(MultipartFile.class))).thenReturn("ocr-result");
        when(ocrService.parseVehicleLicenseBack("ocr-result")).thenReturn(parseResult);
        when(storageService.upload(any(ByteArrayInputStream.class), anyString(), anyString())).thenReturn("license-back-url");
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);

        Vehicle result = vehicleService.uploadLicenseBack(1L, multipartFile);
        assertNotNull(result);
        assertEquals(2, result.getSeatingCapacity());
        assertEquals("18000kg", result.getTotalMass());
        assertEquals("10000kg", result.getRatedLoad());
    }

    @Test
    void uploadInsuranceInfo() {
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(vehicleInsuranceService.addInsurance(any(VehicleInsuranceDTO.class))).thenReturn(new VehicleInsurance());

        Vehicle result = vehicleService.uploadInsuranceInfo(1L, "人保", "123456", "2023-01-01", "2024-01-01");
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void updateMaintenanceStatus() {
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);

        Vehicle result = vehicleService.updateMaintenanceStatus(1L, VehicleStatusEnum.MAINTENANCE.getCode());
        assertNotNull(result);
        assertEquals(VehicleStatusEnum.MAINTENANCE.getCode(), result.getStatus());
    }

    @Test
    void selectBestVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(vehicle);

        BestVehicleQueryDTO query = new BestVehicleQueryDTO();
        query.setCargoWeight(new BigDecimal(5.0));
        query.setVehicleType(1);
        query.setScheduledTime("2023-01-01");
        query.setDriverId(1L);

        List<DriverVehicleInfo> driverVehicles = new ArrayList<>();
        DriverVehicleInfo info = new DriverVehicleInfo();
        info.setVehicleId(1L);
        driverVehicles.add(info);

        Result<List<DriverVehicleInfo>> result = Result.success(new ArrayList<>());
        result.setData(driverVehicles);

        when(vehicleMapper.selectList(any())).thenReturn(vehicles);
        when(driverClient.getCommonVehicles(1L)).thenReturn(result);
        when(vehicleMapper.findBusyVehicleIds(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        List<BestVehicleVO> bestVehicles = vehicleService.selectBestVehicles(query);
        assertNotNull(bestVehicles);
        assertEquals(1, bestVehicles.size());
        assertEquals(1L, bestVehicles.get(0).getId());
        assertEquals("京A12345", bestVehicles.get(0).getVehicleNo());
    }

    @Test
    void getAvailableVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(vehicle);

        when(vehicleMapper.selectList(any())).thenReturn(vehicles);

        List<VehicleVO> result = vehicleService.getAvailableVehicles();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("京A12345", result.get(0).getVehicleNo());
    }

    @Test
    void createVehicleWithPhotos() throws IOException {
        Map<String, String> parseResult = new HashMap<>();
        parseResult.put("plateNumber", "京A12345");

        when(multipartFile.getBytes()).thenReturn(new byte[0]);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(ocrService.recognizeVehicleLicense(any(byte[].class))).thenReturn("ocr-result");
        when(ocrService.parseVehicleLicenseFront("ocr-result")).thenReturn(parseResult);
        when(vehicleMapper.insert(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle savedVehicle = invocation.getArgument(0);
            savedVehicle.setId(1L);
            return 1;
        });
        when(vehicleStatusService.save(any(VehicleStatus.class))).thenReturn(true);
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(storageService.upload(any(InputStream.class), anyString(), anyString())).thenReturn("photo-url");
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);

        Vehicle result = vehicleService.createVehicleWithPhotos("京A12345", 1, multipartFile, multipartFile);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("京A12345", result.getVehicleNo());
    }

    @Test
    void scrapVehicle() {
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(vehicleMapper.deleteById(1L)).thenReturn(1);
        when(vehicleStatusService.getByVehicleId(1L)).thenReturn(vehicleStatus);
        when(vehicleStatusService.updateById(any(VehicleStatus.class))).thenReturn(true);

        boolean result = vehicleService.scrapVehicle(1L);
        assertTrue(result);
    }

    @Test
    void updateVehicleStatus() {
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        when(vehicleMapper.updateById(any(Vehicle.class))).thenReturn(1);
        when(vehicleStatusService.getByVehicleId(1L)).thenReturn(vehicleStatus);
        when(vehicleStatusService.updateById(any(VehicleStatus.class))).thenReturn(true);

        vehicleService.updateVehicleStatus(1L, VehicleStatusEnum.RUNNING.getCode());
        verify(vehicleMapper, times(1)).updateById(any(Vehicle.class));
        verify(vehicleStatusService, times(1)).updateById(any(VehicleStatus.class));
    }

    @Test
    void existsById() {
        when(vehicleMapper.selectById(1L)).thenReturn(vehicle);
        boolean result = vehicleService.existsById(1L);
        assertTrue(result);
    }

    @Test
    void existsById_NotFound() {
        when(vehicleMapper.selectById(1L)).thenReturn(null);
        boolean result = vehicleService.existsById(1L);
        assertFalse(result);
    }
}
