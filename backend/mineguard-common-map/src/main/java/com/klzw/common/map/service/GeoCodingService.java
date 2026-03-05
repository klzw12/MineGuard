package com.klzw.common.map.service;

import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.domain.Poi;

import java.util.List;

public interface GeoCodingService {
    GeoPoint geocode(String address);
    String reverseGeocode(double longitude, double latitude);
    List<Poi> searchPoi(String keyword, double longitude, double latitude, int radius);
}
