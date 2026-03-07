package com.klzw.common.map.service;

import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.domain.Route;

public interface RoutePlanningService {
    Route drivingRoute(GeoPoint origin, GeoPoint destination);
    Route walkingRoute(GeoPoint origin, GeoPoint destination);
    Route cyclingRoute(GeoPoint origin, GeoPoint destination);
}
