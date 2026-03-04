package com.klzw.common.websocket.manager;

import com.klzw.common.websocket.domain.OnlineUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OnlineUserManager {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ONLINE_USER_KEY_PREFIX = "websocket:online:user:";
    private static final String ONLINE_USER_LIST_KEY = "websocket:online:users";
    private static final long ONLINE_EXPIRE_HOURS = 24;

    public OnlineUserManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addOnlineUser(String userId, String username, String role, String sessionId, String clientIp) {
        OnlineUser onlineUser = OnlineUser.builder()
                .userId(userId)
                .username(username)
                .role(role)
                .sessionId(sessionId)
                .connectTime(LocalDateTime.now())
                .lastActiveTime(LocalDateTime.now())
                .clientIp(clientIp)
                .build();
        
        String userKey = ONLINE_USER_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(userKey, onlineUser, ONLINE_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForSet().add(ONLINE_USER_LIST_KEY, userId);
        
        log.info("用户上线: userId={}, username={}, role={}", userId, username, role);
    }

    public void removeOnlineUser(String userId) {
        String userKey = ONLINE_USER_KEY_PREFIX + userId;
        OnlineUser onlineUser = (OnlineUser) redisTemplate.opsForValue().get(userKey);
        
        if (onlineUser != null) {
            redisTemplate.delete(userKey);
            redisTemplate.opsForSet().remove(ONLINE_USER_LIST_KEY, userId);
            log.info("用户下线: userId={}, username={}", userId, onlineUser.getUsername());
        }
    }

    public OnlineUser getOnlineUser(String userId) {
        String userKey = ONLINE_USER_KEY_PREFIX + userId;
        return (OnlineUser) redisTemplate.opsForValue().get(userKey);
    }

    public boolean isOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USER_LIST_KEY, userId));
    }

    public void updateLastActiveTime(String userId) {
        OnlineUser onlineUser = getOnlineUser(userId);
        if (onlineUser != null) {
            onlineUser.updateLastActiveTime();
            String userKey = ONLINE_USER_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(userKey, onlineUser, ONLINE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
    }

    public List<OnlineUser> getAllOnlineUsers() {
        Set<Object> userIds = redisTemplate.opsForSet().members(ONLINE_USER_LIST_KEY);
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userIds.stream()
                .map(userId -> getOnlineUser((String) userId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<OnlineUser> getOnlineUsersByRole(String role) {
        return getAllOnlineUsers().stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }

    public int getOnlineCount() {
        Long size = redisTemplate.opsForSet().size(ONLINE_USER_LIST_KEY);
        return size != null ? size.intValue() : 0;
    }

    public int getOnlineCountByRole(String role) {
        return (int) getAllOnlineUsers().stream()
                .filter(user -> role.equals(user.getRole()))
                .count();
    }

    public Map<String, Object> getOnlineUserStatus(List<String> userIds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> onlineUsers = new ArrayList<>();
        
        for (String userId : userIds) {
            OnlineUser onlineUser = getOnlineUser(userId);
            Map<String, Object> userStatus = new HashMap<>();
            userStatus.put("userId", userId);
            
            if (onlineUser != null) {
                userStatus.put("status", "ONLINE");
                userStatus.put("lastActiveTime", onlineUser.getLastActiveTime());
            } else {
                userStatus.put("status", "OFFLINE");
                userStatus.put("lastActiveTime", null);
            }
            
            onlineUsers.add(userStatus);
        }
        
        result.put("onlineUsers", onlineUsers);
        return result;
    }

    public void clear() {
        Set<Object> userIds = redisTemplate.opsForSet().members(ONLINE_USER_LIST_KEY);
        if (userIds != null) {
            for (Object userId : userIds) {
                redisTemplate.delete(ONLINE_USER_KEY_PREFIX + userId);
            }
        }
        redisTemplate.delete(ONLINE_USER_LIST_KEY);
        log.info("清空所有在线用户");
    }
}
