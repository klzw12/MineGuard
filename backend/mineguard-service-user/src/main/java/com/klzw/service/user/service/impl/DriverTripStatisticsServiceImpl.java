package com.klzw.service.user.service.impl;

import com.klzw.common.core.domain.dto.DriverTripStatistics;
import com.klzw.service.user.service.DriverTripStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverTripStatisticsServiceImpl implements DriverTripStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public DriverTripStatistics getStatisticsByDriverId(Long driverId, LocalDate startDate, LocalDate endDate) {
        log.info("获取司机出车统计：driverId={}, startDate={}, endDate={}", driverId, startDate, endDate);
        
        DriverTripStatistics stats = new DriverTripStatistics();
        stats.setDriverId(driverId);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);
        
        String sql = """
            SELECT 
                COUNT(*) as total_trips,
                SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as success_trips,
                SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as failed_trips,
                COALESCE(SUM(distance), 0) as total_distance,
                COALESCE(SUM(duration), 0) as total_duration,
                COALESCE(SUM(load_weight), 0) as total_load
            FROM trip 
            WHERE driver_id = ? 
            AND DATE(start_time) BETWEEN ? AND ?
            AND deleted = 0
        """;
        
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, driverId, startDate, endDate);
            if (result != null) {
                stats.setTotalTrips(getInt(result, "total_trips"));
                stats.setSuccessTrips(getInt(result, "success_trips"));
                stats.setFailedTrips(getInt(result, "failed_trips"));
                stats.setTotalDistance(getDouble(result, "total_distance"));
                stats.setTotalDuration(getDouble(result, "total_duration"));
                stats.setTotalLoad(getInt(result, "total_load"));
                
                if (stats.getTotalTrips() > 0) {
                    stats.setSuccessRate((double) stats.getSuccessTrips() / stats.getTotalTrips() * 100);
                } else {
                    stats.setSuccessRate(0.0);
                }
            }
        } catch (Exception e) {
            log.error("查询出车统计失败", e);
            setDefaultStats(stats);
        }
        
        String warningSql = """
            SELECT COUNT(*) as warning_count
            FROM warning_record
            WHERE driver_id = ?
            AND DATE(create_time) BETWEEN ? AND ?
            AND deleted = 0
        """;
        
        try {
            Integer warningCount = jdbcTemplate.queryForObject(warningSql, Integer.class, driverId, startDate, endDate);
            stats.setWarningCount(warningCount != null ? warningCount : 0);
        } catch (Exception e) {
            log.warn("查询预警次数失败", e);
            stats.setWarningCount(0);
        }
        
        String violationSql = """
            SELECT COUNT(*) as violation_count
            FROM warning_record
            WHERE driver_id = ?
            AND warning_type IN (3, 4, 5)
            AND DATE(create_time) BETWEEN ? AND ?
            AND deleted = 0
        """;
        
        try {
            Integer violationCount = jdbcTemplate.queryForObject(violationSql, Integer.class, driverId, startDate, endDate);
            stats.setViolationCount(violationCount != null ? violationCount : 0);
        } catch (Exception e) {
            log.warn("查询违规次数失败", e);
            stats.setViolationCount(0);
        }
        
        return stats;
    }

    @Override
    public List<DriverTripStatistics> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("获取日期范围内出车统计：startDate={}, endDate={}", startDate, endDate);
        
        String driverSql = """
            SELECT DISTINCT driver_id FROM driver WHERE deleted = 0 AND status = 1
        """;
        
        List<DriverTripStatistics> result = new ArrayList<>();
        
        try {
            List<Long> driverIds = jdbcTemplate.queryForList(driverSql, Long.class);
            for (Long driverId : driverIds) {
                result.add(getStatisticsByDriverId(driverId, startDate, endDate));
            }
        } catch (Exception e) {
            log.error("获取司机列表失败", e);
        }
        
        return result;
    }

    @Override
    public DriverTripStatistics getMonthlyStatistics(Long driverId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        return getStatisticsByDriverId(driverId, startDate, endDate);
    }

    @Override
    public void updateStatisticsAfterTripComplete(Long driverId, Long tripId, boolean success) {
        log.info("更新出车统计：driverId={}, tripId={}, success={}", driverId, tripId, success);
    }

    @Override
    public void recordTripFailure(Long driverId, Long tripId, String reason) {
        log.warn("记录出车失败：driverId={}, tripId={}, reason={}", driverId, tripId, reason);
    }

    private void setDefaultStats(DriverTripStatistics stats) {
        stats.setTotalTrips(0);
        stats.setSuccessTrips(0);
        stats.setFailedTrips(0);
        stats.setSuccessRate(0.0);
        stats.setTotalDistance(0.0);
        stats.setTotalDuration(0.0);
        stats.setTotalLoad(0);
        stats.setWarningCount(0);
        stats.setViolationCount(0);
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
}
