package com.klzw.service.user.service.impl;

import com.klzw.service.user.entity.Driver;
import com.klzw.service.user.mapper.DriverMapper;
import com.klzw.service.user.service.DriverScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverScoreServiceImpl implements DriverScoreService {

    private final DriverMapper driverMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final int MIN_SCORE = 10;
    private static final int MAX_SCORE = 100;
    private static final int WARNING_THRESHOLD = 10;
    private static final int SEVERE_WARNING_THRESHOLD = 5;
    private static final double VIOLATION_PENALTY_FACTOR = 2.5; // 违规扣分系数（5/2）

    @Override
    @Transactional
    public void initDriverScore(Long driverId, Integer drivingYears) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) {
            throw new RuntimeException("司机不存在");
        }
        
        int baseScore = calculateBaseScore(drivingYears);
        driver.setScore(baseScore);
        driver.setUpdateTime(LocalDateTime.now());
        driverMapper.updateById(driver);
        
        log.info("初始化司机评分：司机ID={}, 驾龄={}, 基准分数={}", driverId, drivingYears, baseScore);
    }

    @Override
    @Transactional
    public void addScoreForAttendance(Long driverId, Integer points) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) return;
        
        // 出勤只加分，不扣分
        int newScore = Math.min(driver.getScore() + points, MAX_SCORE);
        updateDriverScore(driverId, newScore);
        
        log.info("出勤加分：司机 ID={}, 加分={}, 新分数={}", driverId, points, newScore);
    }
    
    /**
     * 根据 Trip 完成情况计算并更新分数
     * 公式：Trip 任务分 = 任务基准分 - 违规分 × 2.5%
     * 
     * @param driverId 司机 ID
     * @param pythonScore Python 计算的驾驶行为评分（0-100，扣分制）
     * @param tripDistance 行程里程（km）
     * @return Trip 任务得分
     */
    @Override
    @Transactional
    public int updateScoreFromTrip(Long driverId, int pythonScore, double tripDistance) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) {
            throw new RuntimeException("司机不存在");
        }
        
        // 1. 计算任务基准分（根据里程等级）
        int taskBaseScore = getTaskBaseScore(tripDistance);
        String mileageLevel = getMileageLevel(tripDistance);
        
        // 2. 计算违规分（Python 得分的扣分部分）
        int violationPoints = 100 - pythonScore;
        
        // 3. 计算 Trip 任务分 = 任务基准分 - 违规分 × 2.5%
        double tripScore = taskBaseScore - (violationPoints * VIOLATION_PENALTY_FACTOR);
        int finalTripScore = (int) Math.round(tripScore);
        
        // 4. 更新司机总分
        int currentScore = driver.getScore();
        int newScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, currentScore + finalTripScore));
        
        driver.setScore(newScore);
        driver.setUpdateTime(LocalDateTime.now());
        driverMapper.updateById(driver);
        
        // 5. 检查是否需要警告
        if (newScore < SEVERE_WARNING_THRESHOLD) {
            sendSevereWarning(driver, "分数过低，请立即整改");
        } else if (newScore < WARNING_THRESHOLD) {
            sendWarning(driver, "分数较低，请注意");
        }
        
        log.info("Trip 评分更新：司机 ID={}, 里程={}km({}), Python 得分={}, 违规分={}, Trip 得分={}, 新总分={}",
                driverId, tripDistance, mileageLevel, pythonScore, violationPoints, finalTripScore, newScore);
        
        return finalTripScore;
    }
    
    /**
     * 根据里程获取任务基准分
     */
    private int getTaskBaseScore(double distance) {
        if (distance < 50) {
            return 10;   // 短途
        } else if (distance < 100) {
            return 20;   // 中途
        } else if (distance < 200) {
            return 35;   // 中长途
        } else if (distance < 500) {
            return 55;   // 长途
        } else {
            return 80;   // 超长途
        }
    }
    
    /**
     * 获取里程等级名称
     */
    private String getMileageLevel(double distance) {
        if (distance < 50) {
            return "短途";
        } else if (distance < 100) {
            return "中途";
        } else if (distance < 200) {
            return "中长途";
        } else if (distance < 500) {
            return "长途";
        } else {
            return "超长途";
        }
    }

    @Override
    @Transactional
    public void addScoreForTripComplete(Long driverId, Integer points) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) return;
        
        int newScore = Math.min(driver.getScore() + points, MAX_SCORE);
        updateDriverScore(driverId, newScore);
        
        log.info("完成任务加分：司机ID={}, 加分={}, 新分数={}", driverId, points, newScore);
    }

    @Override
    @Transactional
    public void deductScoreForViolation(Long driverId, Integer points) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) return;
        
        int newScore = Math.max(driver.getScore() - points, 0);
        updateDriverScore(driverId, newScore);
        
        checkAndSendWarning(driver, newScore);
        
        log.info("违规扣分：司机ID={}, 扣分={}, 新分数={}", driverId, points, newScore);
    }

    @Override
    @Transactional
    public void handleTheftCase(Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) return;
        
        updateDriverScore(driverId, 0);
        
        driver.setStatus(0);
        driver.setUpdateTime(LocalDateTime.now());
        driverMapper.updateById(driver);
        
        sendSevereWarning(driver, "盗泻行为，账户已封禁");
        
        log.warn("盗泻处理：司机ID={}, 分数清零，账户封禁", driverId);
    }

    @Override
    public Integer getDriverScore(Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        return driver != null ? driver.getScore() : null;
    }

    @Override
    @Transactional
    public void updateDriverScore(Long driverId, Integer newScore) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) return;
        
        driver.setScore(newScore);
        driver.setUpdateTime(LocalDateTime.now());
        driverMapper.updateById(driver);
    }

    private int calculateBaseScore(Integer drivingYears) {
        if (drivingYears == null || drivingYears <= 0) {
            return MIN_SCORE;
        }
        
        int baseScore = MIN_SCORE + Math.min(drivingYears, 10) * 3;
        return Math.min(baseScore, 40);
    }

    private void checkAndSendWarning(Driver driver, Integer newScore) {
        if (newScore < SEVERE_WARNING_THRESHOLD) {
            sendSevereWarning(driver, "分数过低，请立即整改");
        } else if (newScore < WARNING_THRESHOLD) {
            sendWarning(driver, "分数较低，请注意");
        }
    }

    private void sendWarning(Driver driver, String message) {
        try {
            rabbitTemplate.convertAndSend(
                "driver.warning.exchange",
                "driver.warning.normal",
                buildWarningMessage(driver, message, "NORMAL")
            );
            log.info("发送普通警告：司机ID={}", driver.getId());
        } catch (Exception e) {
            log.error("发送警告失败", e);
        }
    }

    private void sendSevereWarning(Driver driver, String message) {
        try {
            rabbitTemplate.convertAndSend(
                "driver.warning.exchange",
                "driver.warning.severe",
                buildWarningMessage(driver, message, "SEVERE")
            );
            log.warn("发送严重警告：司机ID={}", driver.getId());
        } catch (Exception e) {
            log.error("发送严重警告失败", e);
        }
    }

    private String buildWarningMessage(Driver driver, String message, String level) {
        return String.format(
            "{\"driverId\":%d,\"driverName\":\"%s\",\"score\":%d,\"level\":\"%s\",\"message\":\"%s\",\"time\":\"%s\"}",
            driver.getId(),
            driver.getDriverName(),
            driver.getScore(),
            level,
            message,
            LocalDateTime.now().toString()
        );
    }
}
