package com.klzw.service.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.vehicle.entity.Vehicle;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface VehicleMapper extends BaseMapper<Vehicle> {

    @Select("SELECT DISTINCT vehicle_id FROM dispatch_task_transport " +
            "WHERE status IN (0, 1, 2) " +
            "AND scheduled_start_time >= #{startTime} " +
            "AND scheduled_start_time <= #{endTime} " +
            "AND deleted = 0")
    List<Long> findBusyVehicleIds(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}
