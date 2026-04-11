package com.klzw.service.vehicle;

import com.klzw.common.core.config.DotenvInitializer;
import com.klzw.service.vehicle.entity.Vehicle;
import com.klzw.service.vehicle.enums.VehicleStatusEnum;
import com.klzw.service.vehicle.service.VehicleService;
import com.klzw.service.vehicle.vo.VehicleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Transactional
public class VehicleIntegrationTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Vehicle vehicle;
    private String uniqueVehicleNo;

    @BeforeEach
    void setUp() {
        // 使用truncate table清理测试数据，避免数据饱满
        jdbcTemplate.execute("TRUNCATE TABLE vehicle");
        jdbcTemplate.execute("TRUNCATE TABLE vehicle_status");
        
        // 使用动态生成的车辆编号，避免车辆编号重复导致的注册失败
        uniqueVehicleNo = "京A" + System.currentTimeMillis();
        vehicle = new Vehicle();
        vehicle.setVehicleNo(uniqueVehicleNo);
        vehicle.setVehicleType(1);
        vehicle.setBrand("解放");
        vehicle.setModel("J6P");
        vehicle.setFuelLevel(80);
        vehicle.setRatedLoad("10000kg");
    }

    @Test
    void testCreateAndGetVehicle() {
        // 创建车辆
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        assertNotNull(createdVehicle);
        assertEquals(uniqueVehicleNo, createdVehicle.getVehicleNo());
        assertEquals(VehicleStatusEnum.IDLE.getCode(), createdVehicle.getStatus());

        // 获取车辆信息
        VehicleVO vehicleVO = vehicleService.getVehicleById(createdVehicle.getId());
        assertNotNull(vehicleVO);
        assertEquals(createdVehicle.getId().toString(), vehicleVO.getId());
        assertEquals(uniqueVehicleNo, vehicleVO.getVehicleNo());
        assertEquals("解放", vehicleVO.getBrand());
        assertEquals("J6P", vehicleVO.getModel());
    }

    @Test
    void testUpdateVehicle() {
        // 创建车辆
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        Long vehicleId = createdVehicle.getId();

        // 使用动态生成的车辆编号，避免车辆编号重复导致的更新失败
        String uniqueUpdatedVehicleNo = "京A" + System.currentTimeMillis();
        
        // 更新车辆信息
        Vehicle updatedVehicle = new Vehicle();
        updatedVehicle.setVehicleNo(uniqueUpdatedVehicleNo);
        updatedVehicle.setBrand("东风");
        updatedVehicle.setModel("天龙");

        Vehicle result = vehicleService.updateVehicle(vehicleId, updatedVehicle);
        assertNotNull(result);
        assertEquals(vehicleId, result.getId());
        assertEquals(uniqueUpdatedVehicleNo, result.getVehicleNo());
        assertEquals("东风", result.getBrand());
        assertEquals("天龙", result.getModel());

        // 验证更新后的数据
        VehicleVO vehicleVO = vehicleService.getVehicleById(vehicleId);
        assertNotNull(vehicleVO);
        assertEquals(uniqueUpdatedVehicleNo, vehicleVO.getVehicleNo());
        assertEquals("东风", vehicleVO.getBrand());
        assertEquals("天龙", vehicleVO.getModel());
    }

    @Test
    void testDeleteVehicle() {
        // 创建车辆
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        Long vehicleId = createdVehicle.getId();

        // 验证车辆存在
        VehicleVO vehicleVO = vehicleService.getVehicleById(vehicleId);
        assertNotNull(vehicleVO);

        // 删除车辆
        boolean deleted = vehicleService.deleteVehicle(vehicleId);
        assertTrue(deleted);

        // 验证车辆不存在
        VehicleVO deletedVehicle = vehicleService.getVehicleById(vehicleId);
        assertNull(deletedVehicle);
    }

    @Test
    void testUpdateVehicleStatus() {
        // 创建车辆
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        Long vehicleId = createdVehicle.getId();

        // 验证初始状态
        VehicleVO initialVehicle = vehicleService.getVehicleById(vehicleId);
        assertEquals(VehicleStatusEnum.IDLE.getCode(), initialVehicle.getStatus());

        // 更新状态为工作中
        vehicleService.updateVehicleStatus(vehicleId, VehicleStatusEnum.RUNNING.getCode());

        // 验证状态已更新
        VehicleVO updatedVehicle = vehicleService.getVehicleById(vehicleId);
        assertEquals(VehicleStatusEnum.RUNNING.getCode(), updatedVehicle.getStatus());

        // 更新状态为空闲
        vehicleService.updateVehicleStatus(vehicleId, VehicleStatusEnum.IDLE.getCode());

        // 验证状态已更新
        VehicleVO idleVehicle = vehicleService.getVehicleById(vehicleId);
        assertEquals(VehicleStatusEnum.IDLE.getCode(), idleVehicle.getStatus());
    }

    @Test
    void testGetAvailableVehicles() {
        // 创建多辆车辆
        for (int i = 0; i < 3; i++) {
            Vehicle newVehicle = new Vehicle();
            newVehicle.setVehicleNo("京A" + System.currentTimeMillis() + i);
            newVehicle.setVehicleType(1);
            newVehicle.setBrand("解放");
            newVehicle.setModel("J6P");
            newVehicle.setFuelLevel(80 - i * 10);
            newVehicle.setRatedLoad("10000kg");
            vehicleService.createVehicle(newVehicle);
        }

        // 获取可用车辆
        List<VehicleVO> availableVehicles = vehicleService.getAvailableVehicles();
        assertNotNull(availableVehicles);
        assertTrue(availableVehicles.size() >= 3);

        // 验证所有车辆状态都是空闲
        for (VehicleVO vehicleVO : availableVehicles) {
            assertEquals(VehicleStatusEnum.IDLE.getCode(), vehicleVO.getStatus());
        }
    }

    @Test
    void testScrapVehicle() {
        // 创建车辆
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        Long vehicleId = createdVehicle.getId();

        // 验证车辆存在
        VehicleVO vehicleVO = vehicleService.getVehicleById(vehicleId);
        assertNotNull(vehicleVO);

        // 报废车辆
        boolean scrapped = vehicleService.scrapVehicle(vehicleId);
        assertTrue(scrapped);

        // 验证车辆不存在或已报废
        VehicleVO scrappedVehicle = vehicleService.getVehicleById(vehicleId);
        assertNull(scrappedVehicle);
    }

    @Test
    void testExistsById() {
        // 创建车辆
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        Long vehicleId = createdVehicle.getId();

        // 验证车辆存在
        boolean exists = vehicleService.existsById(vehicleId);
        assertTrue(exists);

        // 验证不存在的车辆
        boolean notExists = vehicleService.existsById(999L);
        assertFalse(notExists);
    }
}
