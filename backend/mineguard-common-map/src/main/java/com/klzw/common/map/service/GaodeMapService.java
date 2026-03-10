package com.klzw.common.map.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

import lombok.NonNull;
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
                    public GeoFence load(@NonNull String key) {
                        return null;
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

        JSONObject result = client.request("/geocode/geo", params);
        JSONArray geocodes = result.getJSONArray("geocodes");
        
        if (geocodes == null || geocodes.isEmpty()) {
            throw new MapException("No geocoding result found");
        }

        JSONObject geocode = geocodes.getJSONObject(0);
        String location = geocode.getString("location");
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

        JSONObject result = client.request("/geocode/regeo", params);
        JSONObject regeocode = result.getJSONObject("regeocode");
        
        if (regeocode == null) {
            throw new MapException("No reverse geocoding result found");
        }

        return regeocode.getString("formatted_address");
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

        JSONObject result = client.request("/place/text", params);
        JSONArray pois = result.getJSONArray("pois");
        List<Poi> poiList = new ArrayList<>();

        if (pois != null) {
            for (int i = 0; i < pois.size(); i++) {
                JSONObject poiJson = pois.getJSONObject(i);
                Poi poi = new Poi();
                poi.setId(poiJson.getString("id"));
                poi.setName(poiJson.getString("name"));
                poi.setAddress(poiJson.getString("address"));
                poi.setType(poiJson.getString("type"));
                poi.setDistance(poiJson.getDoubleValue("distance"));

                String location = poiJson.getString("location");
                if (location != null) {
                    String[] coords = location.split(",");
                    double poiLon = Double.parseDouble(coords[0]);
                    double poiLat = Double.parseDouble(coords[1]);
                    poi.setLocation(new GeoPoint(poiLon, poiLat));
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
        return calculateRoute(origin, destination, "driving");
    }

    @Override
    public Route walkingRoute(GeoPoint origin, GeoPoint destination) {
        validatePoint(origin);
        validatePoint(destination);
        return calculateRoute(origin, destination, "walking");
    }

    @Override
    public Route cyclingRoute(GeoPoint origin, GeoPoint destination) {
        validatePoint(origin);
        validatePoint(destination);
        return calculateRoute(origin, destination, "bicycling");
    }

    private Route calculateRoute(GeoPoint origin, GeoPoint destination, String mode) {
        Map<String, String> params = new HashMap<>();
        params.put("origin", origin.getLongitude() + "," + origin.getLatitude());
        params.put("destination", destination.getLongitude() + "," + destination.getLatitude());
        params.put("output", "json");
        params.put("strategy", "0");

        String path = "/direction/" + mode;
        JSONObject result = client.request(path, params);
        JSONObject route = result.getJSONObject("route");
        
        if (route == null) {
            throw new MapException("No route found");
        }

        JSONArray paths = route.getJSONArray("paths");
        if (paths == null || paths.isEmpty()) {
            throw new MapException("No path found");
        }

        JSONObject pathObj = paths.getJSONObject(0);
        Route routeResult = new Route();
        routeResult.setOrigin(params.get("origin"));
        routeResult.setDestination(params.get("destination"));
        routeResult.setDistance(pathObj.getDoubleValue("distance"));
        routeResult.setDuration(pathObj.getIntValue("duration"));

        // 解析路径坐标
        String polyline = pathObj.getString("polyline");
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
}
