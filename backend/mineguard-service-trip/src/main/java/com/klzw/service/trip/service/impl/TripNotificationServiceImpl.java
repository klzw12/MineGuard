package com.klzw.service.trip.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.service.trip.entity.TripNotification;
import com.klzw.service.trip.mapper.TripNotificationMapper;
import com.klzw.service.trip.service.TripNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripNotificationServiceImpl implements TripNotificationService {

    private final TripNotificationMapper tripNotificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(TripNotification notification) {
        notification.setStatus("unread");
        notification.setCreateTime(LocalDateTime.now());
        tripNotificationMapper.insert(notification);
        log.info("创建通知成功，通知ID：{}，行程ID：{}，用户ID：{}", 
                notification.getId(), notification.getTripId(), notification.getUserId());
    }

    @Override
    public List<TripNotification> getByTripId(Long tripId) {
        LambdaQueryWrapper<TripNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TripNotification::getTripId, tripId)
               .orderByDesc(TripNotification::getCreateTime);
        return tripNotificationMapper.selectList(wrapper);
    }

    @Override
    public List<TripNotification> getByUserId(Long userId) {
        LambdaQueryWrapper<TripNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TripNotification::getUserId, userId)
               .orderByDesc(TripNotification::getCreateTime);
        return tripNotificationMapper.selectList(wrapper);
    }

    @Override
    public List<TripNotification> getUnreadByUserId(Long userId) {
        LambdaQueryWrapper<TripNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TripNotification::getUserId, userId)
               .eq(TripNotification::getStatus, "unread")
               .orderByDesc(TripNotification::getCreateTime);
        return tripNotificationMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        TripNotification notification = tripNotificationMapper.selectById(id);
        if (notification != null) {
            notification.setStatus("read");
            notification.setReadTime(LocalDateTime.now());
            tripNotificationMapper.updateById(notification);
            log.info("标记通知为已读，通知ID：{}", id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsReadByUserId(Long userId) {
        LambdaQueryWrapper<TripNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TripNotification::getUserId, userId)
               .eq(TripNotification::getStatus, "unread");
        
        List<TripNotification> notifications = tripNotificationMapper.selectList(wrapper);
        for (TripNotification notification : notifications) {
            notification.setStatus("read");
            notification.setReadTime(LocalDateTime.now());
            tripNotificationMapper.updateById(notification);
        }
        
        log.info("标记用户所有通知为已读，用户ID：{}，通知数量：{}", userId, notifications.size());
    }
}
