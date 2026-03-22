package com.klzw.service.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.statistics.entity.FaultStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FaultStatisticsMapper extends BaseMapper<FaultStatistics> {

    @Select("SELECT * FROM fault_statistics WHERE statistics_date = #{date}")
    List<FaultStatistics> findByDate(@Param("date") LocalDate date);

    @Select("SELECT * FROM fault_statistics WHERE vehicle_id = #{vehicleId} ORDER BY statistics_date DESC")
    List<FaultStatistics> findByVehicleId(@Param("vehicleId") Long vehicleId);

    @Select("SELECT * FROM fault_statistics WHERE statistics_date BETWEEN #{startDate} AND #{endDate} ORDER BY statistics_date ASC")
    List<FaultStatistics> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT SUM(fault_count) FROM fault_statistics WHERE statistics_date BETWEEN #{startDate} AND #{endDate}")
    Integer sumFaultCountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT top_fault_type, SUM(top_fault_count) as total_count FROM fault_statistics WHERE statistics_date BETWEEN #{startDate} AND #{endDate} GROUP BY top_fault_type ORDER BY total_count DESC LIMIT #{limit}")
    List<Object[]> findTopFaultTypes(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("limit") int limit);
}
