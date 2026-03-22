package com.klzw.service.dispatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.dispatch.entity.MaintenanceTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MaintenanceTaskMapper extends BaseMapper<MaintenanceTask> {

    @Select("SELECT * FROM dispatch_task_maintenance WHERE plan_id = #{planId} AND deleted = 0 ORDER BY create_time")
    List<MaintenanceTask> findByPlanId(@Param("planId") Long planId);

    @Select("SELECT * FROM dispatch_task_maintenance WHERE vehicle_id = #{vehicleId} AND status IN (0, 1, 2) AND deleted = 0 ORDER BY scheduled_start_time")
    List<MaintenanceTask> findPendingByVehicleId(@Param("vehicleId") Long vehicleId);

    @Select("SELECT * FROM dispatch_task_maintenance WHERE executor_id = #{executorId} AND status IN (0, 1, 2) AND deleted = 0 ORDER BY scheduled_start_time")
    List<MaintenanceTask> findPendingByExecutorId(@Param("executorId") Long executorId);

    @Select("SELECT * FROM dispatch_task_maintenance WHERE executor_id = #{executorId} AND status = 0 AND deleted = 0 ORDER BY scheduled_start_time")
    List<MaintenanceTask> findUnassignedByExecutorId(@Param("executorId") Long executorId);

    @Select("SELECT COUNT(*) FROM dispatch_task_maintenance WHERE executor_id = #{executorId} AND status IN (1, 2) AND deleted = 0 AND ((scheduled_start_time BETWEEN #{startTime} AND #{endTime}) OR (scheduled_end_time BETWEEN #{startTime} AND #{endTime}))")
    int countByExecutorIdAndTimeRange(@Param("executorId") Long executorId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM dispatch_task_maintenance WHERE fault_type = #{faultType} AND deleted = 0 ORDER BY create_time DESC")
    List<MaintenanceTask> findByFaultType(@Param("faultType") Integer faultType);
}
