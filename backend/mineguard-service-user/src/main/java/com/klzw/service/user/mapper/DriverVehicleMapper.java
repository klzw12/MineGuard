package com.klzw.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.user.entity.DriverVehicle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DriverVehicleMapper extends BaseMapper<DriverVehicle> {

    @Select("SELECT * FROM driver_vehicle WHERE driver_id = #{driverId} AND deleted = 0 ORDER BY is_default DESC, use_count DESC")
    List<DriverVehicle> selectByDriverId(@Param("driverId") Long driverId);

    @Select("SELECT * FROM driver_vehicle WHERE driver_id = #{driverId} AND vehicle_id = #{vehicleId} AND deleted = 0")
    DriverVehicle selectByDriverAndVehicle(@Param("driverId") Long driverId, @Param("vehicleId") Long vehicleId);

    @Select("SELECT * FROM driver_vehicle WHERE driver_id = #{driverId} AND is_default = 1 AND deleted = 0")
    DriverVehicle selectDefaultVehicle(@Param("driverId") Long driverId);

    @Update("UPDATE driver_vehicle SET is_default = 0 WHERE driver_id = #{driverId} AND deleted = 0")
    int clearDefaultByDriverId(@Param("driverId") Long driverId);

    @Update("UPDATE driver_vehicle SET use_count = use_count + 1, last_use_time = NOW() WHERE driver_id = #{driverId} AND vehicle_id = #{vehicleId} AND deleted = 0")
    int incrementUseCount(@Param("driverId") Long driverId, @Param("vehicleId") Long vehicleId);
}
