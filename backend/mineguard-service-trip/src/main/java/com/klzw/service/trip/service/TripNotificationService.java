package com.klzw.service.trip.service;

import com.klzw.service.trip.entity.TripNotification;
import java.util.List;

public interface TripNotificationService {

    void createNotification(TripNotification notification);

    List<TripNotification> getByTripId(Long tripId);

    List<TripNotification> getByUserId(Long userId);

    List<TripNotification> getUnreadByUserId(Long userId);

    void markAsRead(Long id);

    void markAllAsReadByUserId(Long userId);
}
