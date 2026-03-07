package com.klzw.common.map.service;

import com.klzw.common.map.domain.GeoFence;
import com.klzw.common.map.domain.GeoPoint;

import java.util.List;

public interface GeoFenceService {
    GeoFence createCircleFence(String name, GeoPoint center, double radius);
    GeoFence createPolygonFence(String name, List<GeoPoint> coordinates);
    boolean isPointInFence(GeoPoint point, GeoFence fence);
    List<GeoFence> getFences();
    void deleteFence(String fenceId);
}
