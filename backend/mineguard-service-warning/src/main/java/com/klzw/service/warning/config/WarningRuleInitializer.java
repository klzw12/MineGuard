package com.klzw.service.warning.config;

import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预警规则初始化器
 * 在生产环境下自动初始化系统预警规则
 * 通过 mineguard.init.data.warning 控制是否启用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarningRuleInitializer implements CommandLineRunner {

    private final WarningRuleMapper warningRuleMapper;

    @Value("${mineguard.init.data.warning:false}")
    private boolean initWarningEnable;

    // 系统预警规则
    private static final List<WarningRuleData> BASE_WARNING_RULES = List.of(
        new WarningRuleData("车辆故障预警", "VEHICLE_FAULT", 1, 1, "故障代码", "车辆故障预警规则，检测车辆上报的故障信息"),
        new WarningRuleData("路线偏离预警", "ROUTE_DEVIATION", 2, 2, "50米", "路线偏离预警规则，检测车辆是否偏离规划路线"),
        new WarningRuleData("危险地带预警", "DANGER_ZONE", 3, 3, "危险区域", "危险地带预警规则，检测车辆是否进入危险区域"),
        new WarningRuleData("速度异常预警", "SPEED_ABNORMAL", 4, 2, "超过限速20%", "速度异常预警规则，检测车辆是否超速行驶"),
        new WarningRuleData("盗卸行为预警", "THEFT_BEHAVIOR", 5, 3, "超声波检测", "盗卸行为预警规则，检测车辆是否有盗卸矿石行为"),
        new WarningRuleData("疲劳驾驶预警", "FATIGUE_DRIVING", 6, 2, "连续驾驶4小时", "疲劳驾驶预警规则，检测司机是否疲劳驾驶")
    );

    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否启用初始化
        if (!initWarningEnable) {
            log.debug("预警规则初始化未启用，设置 mineguard.init.data.warning=true 以启用");
            return;
        }
        
        log.info("开始初始化系统预警规则...");
        
        try {
            int createdCount = 0;
            int existingCount = 0;
            
            for (WarningRuleData ruleData : BASE_WARNING_RULES) {
                if (initWarningRule(ruleData)) {
                    createdCount++;
                } else {
                    existingCount++;
                }
            }
            
            log.info("预警规则初始化完成：创建 {} 个规则，跳过 {} 个已存在规则", createdCount, existingCount);
        } catch (Exception e) {
            log.error("预警规则初始化失败", e);
        }
    }

    /**
     * 初始化预警规则
     */
    private boolean initWarningRule(WarningRuleData ruleData) {
        // 检查规则是否已存在
        WarningRule existingRule = warningRuleMapper.selectByRuleCode(ruleData.ruleCode);
        if (existingRule != null) {
            log.debug("预警规则已存在，跳过：{}({})", ruleData.ruleName, ruleData.ruleCode);
            return false;
        }
        
        // 创建新规则
        WarningRule rule = new WarningRule();
        rule.setRuleName(ruleData.ruleName);
        rule.setRuleCode(ruleData.ruleCode);
        rule.setWarningType(ruleData.warningType);
        rule.setWarningLevel(ruleData.warningLevel);
        rule.setThresholdValue(ruleData.thresholdValue);
        rule.setStatus(1); // 启用状态
        rule.setDescription(ruleData.description);
        
        int result = warningRuleMapper.insert(rule);
        if (result > 0) {
            log.info("预警规则创建成功：{}({})", ruleData.ruleName, ruleData.ruleCode);
            return true;
        } else {
            log.error("预警规则创建失败：{}({})", ruleData.ruleName, ruleData.ruleCode);
            return false;
        }
    }

    /**
     * 预警规则数据
     */
    public record WarningRuleData(
        String ruleName,
        String ruleCode,
        int warningType,
        int warningLevel,
        String thresholdValue,
        String description
    ) {}
}