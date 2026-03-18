package com.klzw.service.vehicle.client;

import com.klzw.common.core.domain.dto.UserInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface UserServiceClient {
    
    @GetExchange("/api/user/{id}")
    Mono<UserInfo> getUserById(@PathVariable("id") Long userId);
    
    @GetExchange("/api/user/exists/{id}")
    Mono<Boolean> existsById(@PathVariable("id") Long userId);
}