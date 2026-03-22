package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.UserInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserClient {

    @GetExchange("/api/user/{id}")
    UserInfo getUserById(@PathVariable("id") Long userId);

    @GetExchange("/api/user/exists/{id}")
    Boolean existsById(@PathVariable("id") Long userId);
}
