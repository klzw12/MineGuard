package com.klzw.common.map.service;

import com.klzw.common.map.client.GaodeMapClient;
import com.klzw.common.map.domain.GeoFence;
import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.domain.Poi;
import com.klzw.common.map.domain.Route;
import com.klzw.common.map.exception.MapException;
import com.klzw.common.map.properties.GaodeMapProperties;
import com.klzw.common.map.util.GeoUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GaodeMapService implements GeoCodingService, RoutePlanningService, GeoFenceService {
    private final GaodeMapClient client;
    private final LoadingCache<String, GeoFence> fenceCache;

    public GaodeMapService(GaodeMapClient client, GaodeMapProperties properties) {
        this.client = client;
        this.fenceCache = CacheBuilder.newBuilder()
                .expireAfterWrite(properties.getCacheExpire(), TimeUnit.SECONDS)
                .build(new CacheLoader<String, GeoFence>() {
                    @Override
                    public GeoFence load(@Nonnull String key) {
                        throw new MapException("GeoFence not found for key: " + key);
                    }
                });
    }

    @Override
    public GeoPoint geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new MapException("Address cannot be empty");
        }
        
        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        params.put("output", "json");

        Map<String, Object> result = client.request("/geocode/geo", params);
        Object geocodesObj = result.get("geocodes");
        if (!(geocodesObj instanceof List)) {
            throw new MapException("Invalid geocodes format");
        }
        List<Map<String, Object>> geocodes = (List<Map<String, Object>>) geocodesObj;
        
        if (geocodes == null || geocodes.isEmpty()) {
            throw new MapException("No geocoding result found");
        }

        Map<String, Object> geocode = geocodes.get(0);
        String location = (String) geocode.get("location");
        if (location == null) {
            throw new MapException("Location not found in geocode result");
        }
        String[] coords = location.split(",");
        double longitude = Double.parseDouble(coords[0]);
        double latitude = Double.parseDouble(coords[1]);

        return new GeoPoint(longitude, latitude);
    }

    @Override
    public String reverseGeocode(double longitude, double latitude) {
        validateCoordinate(longitude, latitude);
        
        Map<String, String> params = new HashMap<>();
        params.put("location", longitude + "," + latitude);
        params.put("output", "json");

        Map<String, Object> result = client.request("/geocode/regeo", params);
        Object regeocodeObj = result.get("regeocode");
        if (!(regeocodeObj instanceof Map)) {
            throw new MapException("Invalid regeocode format");
        }
        Map<String, Object> regeocode = (Map<String, Object>) regeocodeObj;

        String formattedAddress = (String) regeocode.get("formatted_address");
        if (formattedAddress == null) {
            throw new MapException("Formatted address not found in regeocode result");
        }
        return formattedAddress;
    }


    @Override
    public List<Poi> searchPoi(String keyword, double longitude, double latitude, int radius) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new MapException("Keyword cannot be empty");
        }
        validateCoordinate(longitude, latitude);
        
        if (radius <= 0 || radius > 50000) {
            throw new MapException("Radius must be between 1 and 50000 meters");
        }

        Map<String, String> params = new HashMap<>();
        params.put("keywords", keyword);
        params.put("location", longitude + "," + latitude);
        params.put("radius", String.valueOf(radius));
        params.put("output", "json");
        params.put("page", "1");
        params.put("offset", "20");

        Map<String, Object> result = client.request("/place/text", params);
        Object poisObj = result.get("pois");
        List<Poi> poiList = new ArrayList<>();

        if (poisObj instanceof List) {
            List<Map<String, Object>> pois = (List<Map<String, Object>>) poisObj;
            for (Map<String, Object> poiJson : pois) {
                Poi poi = new Poi();
                poi.setId((String) poiJson.get("id"));
                poi.setName((String) poiJson.get("name"));
                poi.setAddress((String) poiJson.get("address"));
                poi.setType((String) poiJson.get("type"));
                
                Object distanceObj = poiJson.get("distance");
                if (distanceObj != null) {
                    try {
                        poi.setDistance(Double.parseDouble(distanceObj.toString()));
                    } catch (NumberFormatException e) {
                        poi.setDistance(0.0);
                    }
                } else {
                    poi.setDistance(0.0);
                }

                String location = (String) poiJson.get("location");
                if (location != null) {
                    String[] coords = location.split(",");
                    if (coords.length == 2) {
                        try {
                            double poiLon = Double.parseDouble(coords[0]);
                            double poiLat = Double.parseDouble(coords[1]);
                            poi.setLocation(new GeoPoint(poiLon, poiLat));
                        } catch (NumberFormatException e) {
                            // 坐标解析失败，不设置位置
                        }
                    }
                }

                poiList.add(poi);
            }
        }

        return poiList;
    }

    @Override
    public Route drivingRoute(GeoPoint origin, GeoPoint destination) {
        validatePoint(origin);
        validatePoint(destination);
        return calculateRoute(origin, destination, "driving", 0);
    }

    @Override
    public Route drivingRoute(GeoPoint origin, GeoPoint destination, Integer strategy) {
        validatePoint(origin);
        validatePoint(destination);
        return calculateRoute(origin, destination, "driving", strategy);
    }

    @Override
    public Route walkingRoute(GeoPoint origin, GeoPoint destination) {
        validatePoint(origin);
        validatePoint(destination);
        return calculateRoute(origin, destination, "walking", 0);
    }

    @Override
    public Route cyclingRoute(GeoPoint origin, GeoPoint destination) {
        validatePoint(origin);
        validatePoint(destination);
        return calculateRoute(origin, destination, "bicycling", 0);
    }

    private Route calculateRoute(GeoPoint origin, GeoPoint destination, String mode, Integer strategy) {
        Map<String, String> params = new HashMap<>();
        params.put("origin", origin.getLongitude() + "," + origin.getLatitude());
        params.put("destination", destination.getLongitude() + "," + destination.getLatitude());
        params.put("output", "json");
        params.put("strategy", strategy.toString());

        String path = "/direction/" + mode;
        Map<String, Object> result = client.request(path, params);
        Object routeObj = result.get("route");
        if (!(routeObj instanceof Map)) {
            throw new MapException("Invalid route format");
        }
        Map<String, Object> route = (Map<String, Object>) routeObj;

        Object pathsObj = route.get("paths");
        if (!(pathsObj instanceof List)) {
            throw new MapException("Invalid paths format");
        }
        List<Map<String, Object>> paths = (List<Map<String, Object>>) pathsObj;
        if (paths == null || paths.isEmpty()) {
            throw new MapException("No path found");
        }

        Map<String, Object> pathObj = paths.get(0);
        Route routeResult = new Route();
        routeResult.setOrigin(params.get("origin"));
        routeResult.setDestination(params.get("destination"));
        
        Object distanceObj = pathObj.get("distance");
        if (distanceObj != null) {
            try {
                routeResult.setDistance(Double.parseDouble(distanceObj.toString()));
            } catch (NumberFormatException e) {
                routeResult.setDistance(0.0);
            }
        } else {
            routeResult.setDistance(0.0);
        }
        
        Object durationObj = pathObj.get("duration");
        if (durationObj != null) {
            try {
                routeResult.setDuration(Integer.parseInt(durationObj.toString()));
            } catch (NumberFormatException e) {
                routeResult.setDuration(0);
            }
        } else {
            routeResult.setDuration(0);
        }

        // 解析路径坐标
        String polyline = (String) pathObj.get("polyline");
        List<GeoPoint> pathPoints = parsePolyline(polyline);
        routeResult.setPath(pathPoints);

        return routeResult;
    }

    private List<GeoPoint> parsePolyline(String polyline) {
        List<GeoPoint> points = new ArrayList<>();
        String[] coords = polyline.split(";" );
        for (String coord : coords) {
            String[] xy = coord.split(",");
            if (xy.length == 2) {
                double lon = Double.parseDouble(xy[0]);
                double lat = Double.parseDouble(xy[1]);
                points.add(new GeoPoint(lon, lat));
            }
        }
        return points;
    }

    @Override
    public GeoFence createCircleFence(String name, GeoPoint center, double radius) {
        if (name == null || name.trim().isEmpty()) {
            throw new MapException("Fence name cannot be empty");
        }
        validatePoint(center);
        if (radius <= 0 || radius > 50000) {
            throw new MapException("Radius must be between 1 and 50000 meters");
        }

        GeoFence fence = new GeoFence();
        fence.setId("fence_" + System.currentTimeMillis());
        fence.setName(name);
        fence.setType("circle");
        fence.setRadius(radius);
        
        List<GeoPoint> coordinates = new ArrayList<>();
        coordinates.add(center);
        fence.setCoordinates(coordinates);
        
        fenceCache.put(fence.getId(), fence);
        return fence;
    }

    @Override
    public GeoFence createPolygonFence(String name, List<GeoPoint> coordinates) {
        if (name == null || name.trim().isEmpty()) {
            throw new MapException("Fence name cannot be empty");
        }
        if (coordinates == null || coordinates.size() < 3) {
            throw new MapException("Polygon fence must have at least 3 points");
        }
        for (GeoPoint point : coordinates) {
            validatePoint(point);
        }

        GeoFence fence = new GeoFence();
        fence.setId("fence_" + System.currentTimeMillis());
        fence.setName(name);
        fence.setType("polygon");
        fence.setCoordinates(coordinates);
        
        fenceCache.put(fence.getId(), fence);
        return fence;
    }

    @Override
    public boolean isPointInFence(GeoPoint point, GeoFence fence) {
        validatePoint(point);
        if (fence == null) {
            throw new MapException("Fence cannot be null");
        }

        if ("circle".equals(fence.getType())) {
            GeoPoint center = fence.getCoordinates().get(0);
            double distance = GeoUtils.calculateDistance(point, center);
            return distance <= fence.getRadius();
        } else if ("polygon".equals(fence.getType())) {
            return isPointInPolygon(point, fence.getCoordinates());
        }
        return false;
    }

    @Override
    public List<GeoFence> getFences() {
        return new ArrayList<>(fenceCache.asMap().values());
    }

    @Override
    public void deleteFence(String fenceId) {
        if (fenceId == null || fenceId.trim().isEmpty()) {
            throw new MapException("Fence ID cannot be empty");
        }
        fenceCache.invalidate(fenceId);
    }

    private boolean isPointInPolygon(GeoPoint point, List<GeoPoint> polygon) {
        int n = polygon.size();
        boolean inside = false;
        double x = point.getLongitude();
        double y = point.getLatitude();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygon.get(i).getLongitude();
            double yi = polygon.get(i).getLatitude();
            double xj = polygon.get(j).getLongitude();
            double yj = polygon.get(j).getLatitude();

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    private void validateCoordinate(double longitude, double latitude) {
        if (longitude < -180 || longitude > 180) {
            throw new MapException("Longitude must be between -180 and 180");
        }
        if (latitude < -90 || latitude > 90) {
            throw new MapException("Latitude must be between -90 and 90");
        }
    }

    private void validatePoint(GeoPoint point) {
        if (point == null) {
            throw new MapException("GeoPoint cannot be null");
        }
        validateCoordinate(point.getLongitude(), point.getLatitude());
    }
    
    public List<Poi> inputTips(String keyword, String city) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new MapException("Keyword cannot be empty");
        }

        Map<String, String> params = new HashMap<>();
        params.put("keywords", keyword);
        params.put("datatype", "all");
        params.put("output", "json");
        if (city != null && !city.trim().isEmpty()) {
            params.put("city", city);
        }

        Map<String, Object> result = client.request("/assistant/inputtips", params);
        Object tipsObj = result.get("tips");
        List<Poi> poiList = new ArrayList<>();

        if (tipsObj instanceof List) {
            List<Map<String, Object>> tips = (List<Map<String, Object>>) tipsObj;
            for (Map<String, Object> tip : tips) {
                String location = (String) tip.get("location");
                if (location == null || location.isEmpty()) {
                    continue;
                }
                
                String[] coords = location.split(",");
                if (coords.length != 2) {
                    continue;
                }
                
                Poi poi = new Poi();
                poi.setId((String) tip.get("id"));
                poi.setName((String) tip.get("name"));
                poi.setAddress((String) tip.get("address"));
                if (poi.getAddress() == null || poi.getAddress().isEmpty()) {
                    poi.setAddress((String) tip.get("district"));
                }
                poi.setType((String) tip.get("type"));
                
                try {
                    double poiLon = Double.parseDouble(coords[0]);
                    double poiLat = Double.parseDouble(coords[1]);
                    poi.setLocation(new GeoPoint(poiLon, poiLat));
                } catch (NumberFormatException e) {
                    continue;
                }

                poiList.add(poi);
            }
        }

        return poiList;
    }
}
