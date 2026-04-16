package com.klzw.service.cost.config;

import com.klzw.service.cost.config.SalaryConfigProperties;
import com.klzw.service.cost.service.CostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 定时任务配置
 * 用于自动计算薪酬
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    private CostService costService;
    
    @Autowired
    private SalaryConfigProperties salaryConfigProperties;

    /**
     * 每月1日凌晨2点自动计算上月薪酬
     * 表达式：0 0 2 1 * ?
     */
    @Scheduled(cron = "${mineguard.salary.salary-calculate-cron:0 0 2 1 * ?}")
    public void autoCalculateSalary() {
        try {
            // 获取上月的年月
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            String yearMonth = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            System.out.println("【定时任务】开始自动计算上月薪酬：" + yearMonth);
            
            // 调用按月计算薪酬的方法
            costService.calculateSalariesByMonth(yearMonth);
            
            System.out.println("【定时任务】上月薪酬计算完成：" + yearMonth);
            
        } catch (Exception e) {
            System.err.println("【定时任务】自动计算薪酬失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 每月1日凌晨1点自动清空绩效
     * 表达式：0 0 1 1 * ?
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void autoResetPerformance() {
        try {
            System.out.println("【定时任务】开始自动清空绩效");
            
            // 调用清空绩效的方法
            costService.resetPerformance();
            
            System.out.println("【定时任务】绩效清空完成");
            
        } catch (Exception e) {
            System.err.println("【定时任务】清空绩效失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

}