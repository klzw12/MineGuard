package com.klzw.service.warning.service;

import com.klzw.common.core.client.MessageClient;
import com.klzw.service.warning.dto.WarningTrackDTO;
import com.klzw.service.warning.entity.WarningRecord;
import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import com.klzw.service.warning.processor.EventTriggerProcessor;
import com.klzw.service.warning.processor.WarningTriggerProcessor;
import com.klzw.service.warning.service.impl.WarningServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarningServiceImplTest {

    @Mock
    private EventTriggerProcessor eventTriggerProcessor;

    @Mock
    private WarningTriggerProcessor warningTriggerProcessor;

    @Mock
    private MessageClient messageClient;

    @Mock
    private WarningRuleMapper warningRuleMapper;

    @InjectMocks
    private WarningServiceImpl warningService;

    private WarningTrackDTO warningTrackDTO;
    private WarningRecord warningRecord;
    private WarningRule warningRule;

    @BeforeEach
    void setUp() {
        warningTrackDTO = new WarningTrackDTO();
        warningTrackDTO.setTripId(1L);
        warningTrackDTO.setVehicleId(1L);
        warningTrackDTO.setDriverId(1L);
        warningTrackDTO.setLongitude(116.0);
        warningTrackDTO.setLatitude(39.0);

        warningRecord = new WarningRecord();
        warningRecord.setId(1L);
        warningRecord.setWarningNo("WARN202401010001");
        warningRecord.setTripId(1L);
        warningRecord.setVehicleId(1L);
        warningRecord.setDriverId(1L);
        warningRecord.setWarningType(WarningTypeEnum.SPEED_ABNORMAL.getCode());
        warningRecord.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
        warningRecord.setWarningContent("超速预警");
        warningRecord.setRuleId(1L);

        warningRule = new WarningRule();
        warningRule.setId(1L);
        warningRule.setRuleName("超速预警规则");
        warningRule.setPushRoles("ROLE_SAFETY_OFFICER, ROLE_REPAIRMAN");
    }

    @Test
    void testProcessEventTrigger() {
        // 模拟依赖方法的返回值
        when(eventTriggerProcessor.processEventTrigger(any(WarningTrackDTO.class), anyString())).thenReturn(warningRecord);

        // 调用被测方法
        WarningRecord result = warningService.processEventTrigger(warningTrackDTO, "SPEED_ABNORMAL");

        // 验证结果
        assertNotNull(result);
        assertEquals(warningRecord, result);
        // 验证依赖方法被调用
        verify(eventTriggerProcessor, times(1)).processEventTrigger(any(WarningTrackDTO.class), anyString());
        verify(messageClient, times(1)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessWarningTrack() {
        // 模拟依赖方法的返回值
        when(warningTriggerProcessor.processTrack(any(WarningTrackDTO.class))).thenReturn(warningRecord);

        // 调用被测方法
        WarningRecord result = warningService.processWarningTrack(warningTrackDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(warningRecord, result);
        // 验证依赖方法被调用
        verify(warningTriggerProcessor, times(1)).processTrack(any(WarningTrackDTO.class));
        verify(messageClient, times(1)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testProcessWarningTrackWithNullRecord() {
        // 模拟依赖方法返回null
        when(warningTriggerProcessor.processTrack(any(WarningTrackDTO.class))).thenReturn(null);

        // 调用被测方法
        WarningRecord result = warningService.processWarningTrack(warningTrackDTO);

        // 验证结果
        assertNull(result);
        // 验证依赖方法被调用
        verify(warningTriggerProcessor, times(1)).processTrack(any(WarningTrackDTO.class));
        // 验证保存和推送方法未被调用
        verify(messageClient, never()).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, never()).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testSaveWarningRecord() {
        // 调用被测方法
        boolean result = warningService.saveWarningRecord(warningRecord);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testUpdateWarningStatus() {
        // 调用被测方法
        boolean result = warningService.updateWarningStatus(1L, 1);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testPushWarningNotificationWithRule() {
        // 模拟依赖方法的返回值
        when(warningRuleMapper.selectById(anyLong())).thenReturn(warningRule);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(warningRuleMapper, times(1)).selectById(anyLong());
        verify(messageClient, times(2)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithoutRule() {
        // 模拟依赖方法返回null
        when(warningRuleMapper.selectById(anyLong())).thenReturn(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(warningRuleMapper, times(1)).selectById(anyLong());
        verify(messageClient, times(1)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithNullRuleId() {
        // 设置warningRecord的ruleId为null
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(warningRuleMapper, never()).selectById(anyLong());
        verify(messageClient, times(1)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithNullWarningType() {
        // 设置warningRecord的warningType为null
        warningRecord.setWarningType(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(warningRuleMapper, times(1)).selectById(anyLong());
        verify(messageClient, times(2)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testGetWarningStatistics() {
        // 调用被测方法
        Map<String, Object> statistics = warningService.getWarningStatistics("2024-01-01", "2024-01-31");

        // 验证结果
        assertNotNull(statistics);
        assertEquals(0, statistics.get("totalCount"));
        assertEquals(0, statistics.get("lowLevelCount"));
        assertEquals(0, statistics.get("mediumLevelCount"));
        assertEquals(0, statistics.get("highLevelCount"));
        assertEquals(0, statistics.get("pendingCount"));
        assertEquals(0, statistics.get("handledCount"));
    }

    @Test
    void testGetWarningTrend() {
        // 调用被测方法
        List<Map<String, Object>> trend = warningService.getWarningTrend(7);

        // 验证结果
        assertNotNull(trend);
        assertEquals(7, trend.size());
        for (Map<String, Object> dayData : trend) {
            assertNotNull(dayData.get("date"));
            assertEquals(0, dayData.get("count"));
        }
    }

    @Test
    void testPushWarningNotificationWithVehicleFaultType() {
        // 设置warningRecord的warningType为车辆故障
        warningRecord.setWarningType(WarningTypeEnum.VEHICLE_FAULT.getCode());
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(messageClient, times(1)).sendMessageByRole("ROLE_REPAIRMAN", anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithSpeedAbnormalType() {
        // 设置warningRecord的warningType为超速异常
        warningRecord.setWarningType(WarningTypeEnum.SPEED_ABNORMAL.getCode());
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithDangerZoneType() {
        // 设置warningRecord的warningType为危险区域
        warningRecord.setWarningType(WarningTypeEnum.DANGER_ZONE.getCode());
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(messageClient, times(2)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(2)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithRouteDeviationType() {
        // 设置warningRecord的warningType为路线偏离
        warningRecord.setWarningType(WarningTypeEnum.ROUTE_DEVIATION.getCode());
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(messageClient, times(1)).sendMessageByRole("ROLE_REPAIRMAN", anyString(), anyString(), anyString());
        verify(messageClient, times(2)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithHighLevel() {
        // 设置warningRecord的warningLevel为高危
        warningRecord.setWarningLevel(WarningLevelEnum.HIGH.getCode());
        warningRecord.setWarningType(null);
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(messageClient, times(2)).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithMediumLevel() {
        // 设置warningRecord的warningLevel为中危
        warningRecord.setWarningLevel(WarningLevelEnum.MEDIUM.getCode());
        warningRecord.setWarningType(null);
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法被调用
        verify(messageClient, times(1)).sendMessageByRole("ROLE_REPAIRMAN", anyString(), anyString(), anyString());
        verify(messageClient, times(1)).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPushWarningNotificationWithLowLevel() {
        // 设置warningRecord的warningLevel为低危
        warningRecord.setWarningLevel(WarningLevelEnum.LOW.getCode());
        warningRecord.setWarningType(null);
        warningRecord.setRuleId(null);

        // 调用被测方法
        warningService.pushWarningNotification(warningRecord);

        // 验证依赖方法未被调用
        verify(messageClient, never()).sendMessageByRole(anyString(), anyString(), anyString(), anyString());
        verify(messageClient, never()).sendMessage(anyLong(), anyString(), anyString(), anyString(), anyString());
    }
}
