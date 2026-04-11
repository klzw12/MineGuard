package com.klzw.service.dispatch;

import com.klzw.common.core.config.DotenvInitializer;
import com.klzw.service.dispatch.entity.TransportTask;
import com.klzw.service.dispatch.service.DispatchService;
import com.klzw.service.dispatch.vo.DispatchTaskVO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Transactional
public class DispatchIntegrationTest {

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TransportTask transportTask;

    @BeforeEach
    void setUp() {
        // 使用truncate table清理测试数据，避免数据饱满
        jdbcTemplate.execute("TRUNCATE TABLE dispatch_task_transport");
        jdbcTemplate.execute("TRUNCATE TABLE dispatch_task_maintenance");
        jdbcTemplate.execute("TRUNCATE TABLE dispatch_task_inspection");

        transportTask = new TransportTask();
        // 使用动态生成的任务编号，避免任务编号重复导致的错误
        transportTask.setTaskNo("TASK" + System.currentTimeMillis());
        transportTask.setStatus(0);
        transportTask.setPriority("normal");
        transportTask.setCargoWeight(new BigDecimal(10));
        transportTask.setScheduledStartTime(LocalDateTime.now());
        transportTask.setScheduledEndTime(LocalDateTime.now().plusHours(2));
        transportTask.setStartLocation("起点");
        transportTask.setEndLocation("终点");
    }

    @Test
    void testCreateAndGetDispatchTask() {
        // 创建调度任务
        TransportTask createdTask = dispatchService.createDispatchTask(transportTask);
        assertNotNull(createdTask);
        assertNotNull(createdTask.getId());
        assertEquals(transportTask.getTaskNo(), createdTask.getTaskNo());
        assertEquals(0, createdTask.getStatus());

        // 获取调度任务
        TransportTask retrievedTask = dispatchService.getDispatchTask(createdTask.getId());
        assertNotNull(retrievedTask);
        assertEquals(createdTask.getId(), retrievedTask.getId());
        assertEquals(transportTask.getTaskNo(), retrievedTask.getTaskNo());
    }

    @Test
    void testUpdateDispatchTask() {
        // 创建调度任务
        TransportTask createdTask = dispatchService.createDispatchTask(transportTask);
        Long taskId = createdTask.getId();

        // 更新调度任务
        createdTask.setPriority("high");
        createdTask.setCargoWeight(new BigDecimal(15));
        TransportTask updatedTask = dispatchService.updateDispatchTask(createdTask);
        assertNotNull(updatedTask);
        assertEquals(taskId, updatedTask.getId());
        assertEquals("high", updatedTask.getPriority());
        assertEquals(0, new BigDecimal(15).compareTo(updatedTask.getCargoWeight()));

        // 验证更新后的数据
        TransportTask retrievedTask = dispatchService.getDispatchTask(taskId);
        assertNotNull(retrievedTask);
        assertEquals("high", retrievedTask.getPriority());
        assertEquals(0, new BigDecimal(15).compareTo(retrievedTask.getCargoWeight()));
    }

    @Test
    void testDeleteDispatchTask() {
        // 创建调度任务
        TransportTask createdTask = dispatchService.createDispatchTask(transportTask);
        Long taskId = createdTask.getId();

        // 验证任务存在
        TransportTask retrievedTask = dispatchService.getDispatchTask(taskId);
        assertNotNull(retrievedTask);

        // 删除任务
        boolean deleted = dispatchService.deleteDispatchTask(taskId);
        assertTrue(deleted);

        // 验证任务不存在
        TransportTask deletedTask = dispatchService.getDispatchTask(taskId);
        assertNull(deletedTask);
    }

    @Test
    void testCancelDispatchTask() {
        // 创建调度任务
        TransportTask createdTask = dispatchService.createDispatchTask(transportTask);
        Long taskId = createdTask.getId();

        // 验证任务状态
        assertEquals(0, createdTask.getStatus());

        // 取消任务
        boolean cancelled = dispatchService.cancelDispatchTask(taskId);
        assertTrue(cancelled);

        // 验证任务状态已更新
        TransportTask cancelledTask = dispatchService.getDispatchTask(taskId);
        assertNotNull(cancelledTask);
        assertEquals(4, cancelledTask.getStatus()); // 4表示已取消
    }

    @Test
    void testAssignVehicleAndDriver() {
        // 创建调度任务
        TransportTask createdTask = dispatchService.createDispatchTask(transportTask);
        Long taskId = createdTask.getId();

        // 分配车辆
        dispatchService.assignVehicle(taskId, 1L);
        TransportTask taskWithVehicle = dispatchService.getDispatchTask(taskId);
        assertEquals(1L, taskWithVehicle.getVehicleId());

        // 分配司机
        dispatchService.assignDriver(taskId, 1L);
        TransportTask taskWithDriver = dispatchService.getDispatchTask(taskId);
        assertEquals(1L, taskWithDriver.getExecutorId());
    }

    @Test
    void testCompleteTask() {
        // 创建调度任务
        TransportTask createdTask = dispatchService.createDispatchTask(transportTask);
        Long taskId = createdTask.getId();

        // 完成任务
        dispatchService.completeTask(taskId);

        // 验证任务状态
        TransportTask completedTask = dispatchService.getDispatchTask(taskId);
        assertNotNull(completedTask);
        assertEquals(3, completedTask.getStatus()); // 3表示已完成
        assertNotNull(completedTask.getActualEndTime());
    }

    @Test
    void testReassignTask() {
        // 创建调度任务
        TransportTask createdTask = dispatchService.createDispatchTask(transportTask);
        Long taskId = createdTask.getId();

        // 分配初始车辆和司机
        dispatchService.assignVehicle(taskId, 1L);
        dispatchService.assignDriver(taskId, 1L);

        // 重新分配任务
        dispatchService.reassignTask(taskId, 2L, 2L);

        // 验证重新分配结果
        TransportTask reassignedTask = dispatchService.getDispatchTask(taskId);
        assertNotNull(reassignedTask);
        assertEquals(2L, reassignedTask.getVehicleId());
        assertEquals(2L, reassignedTask.getExecutorId());
    }

    @Test
    void testGetDispatchTaskList() {
        // 创建多个调度任务
        for (int i = 0; i < 3; i++) {
            TransportTask task = new TransportTask();
            // 使用动态生成的任务编号，避免任务编号重复导致的错误
            task.setTaskNo("TASK" + System.currentTimeMillis() + i);
            task.setStatus(i % 2); // 0或1
            task.setPriority("normal");
            task.setCargoWeight(new BigDecimal(10));
            task.setScheduledStartTime(LocalDateTime.now());
            task.setScheduledEndTime(LocalDateTime.now().plusHours(2));
            task.setStartLocation("起点" + i);
            task.setEndLocation("终点" + i);
            dispatchService.createDispatchTask(task);
        }

        // 获取任务列表
        List<TransportTask> tasks = dispatchService.getDispatchTaskList(null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNotNull(tasks);
        assertTrue(tasks.size() >= 3);

        // 按状态获取任务列表
        List<TransportTask> pendingTasks = dispatchService.getDispatchTaskList(0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNotNull(pendingTasks);
        for (TransportTask task : pendingTasks) {
            assertEquals(0, task.getStatus());
        }
    }

    @Test
    void testGetAllTaskList() {
        // 创建调度任务
        dispatchService.createDispatchTask(transportTask);

        // 获取所有任务列表
        List<DispatchTaskVO> tasks = dispatchService.getAllTaskList(null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertNotNull(tasks);
        assertTrue(tasks.size() >= 1);
    }
}

