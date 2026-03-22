package com.klzw.service.user.service;

public interface DriverScoreService {

    void initDriverScore(Long driverId, Integer drivingYears);

    void addScoreForAttendance(Long driverId, Integer points);

    void addScoreForTripComplete(Long driverId, Integer points);

    void deductScoreForViolation(Long driverId, Integer points);

    void handleTheftCase(Long driverId);

    Integer getDriverScore(Long driverId);

    void updateDriverScore(Long driverId, Integer newScore);
    
    /**
     * 根据 Trip 完成情况计算并更新分数
     * 公式：Trip 任务分 = 任务基准分 - 违规分 × 2.5%
     * 
     * @param driverId 司机 ID
     * @param pythonScore Python 计算的驾驶行为评分（0-100，扣分制）
     * @param tripDistance 行程里程（km）
     * @return Trip 任务得分
     */
    int updateScoreFromTrip(Long driverId, int pythonScore, double tripDistance);
}
