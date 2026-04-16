package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.common.redis.service.RedisCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user/location")
@RequiredArgsConstructor
@Tag(name = "用户位置", description = "用户位置相关接口")
public class UserLocationController {

    private final RedisCacheService redisCacheService;
    private static final String USER_LOCATION_PREFIX = "user:location:";
    private static final int LOCATION_EXPIRE_DAYS = 2;

    @PostMapping("/report")
    @Operation(summary = "上报用户位置")
    public Result<Void> reportLocation(
            @RequestParam Long userId,
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(required = false) Double speed,
            @RequestParam(required = false) Double direction) {
        
        String locationKey = USER_LOCATION_PREFIX + userId;
        
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("longitude", longitude);
        locationData.put("latitude", latitude);
        locationData.put("speed", speed);
        locationData.put("direction", direction);
        locationData.put("timestamp", System.currentTimeMillis());
        
        redisCacheService.set(locationKey, locationData, LOCATION_EXPIRE_DAYS, TimeUnit.DAYS);
        
        log.debug("用户位置上报成功：userId={}, 位置=({}, {})", userId, longitude, latitude);
        return Result.success();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户位置")
    public Result<Map<String, Object>> getUserLocation(@PathVariable Long userId) {
        String locationKey = USER_LOCATION_PREFIX + userId;
        Map<String, Object> locationData = redisCacheService.get(locationKey);
        
        if (locationData == null) {
            return Result.success(null);
        }
        
        return Result.success(locationData);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "清除用户位置")
    public Result<Void> clearUserLocation(@PathVariable Long userId) {
        String locationKey = USER_LOCATION_PREFIX + userId;
        redisCacheService.delete(locationKey);
        
        log.info("用户位置已清除：userId={}", userId);
        return Result.success();
    }
}
