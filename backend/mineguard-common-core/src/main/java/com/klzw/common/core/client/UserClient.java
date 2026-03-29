package com.klzw.common.core.client;

import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserClient {

    @GetExchange("/user/exists/{id}")
    Boolean existsUser(@PathVariable("id") Long id);

    @GetExchange("/user/{id}")
    Result<Object> getUserById(@PathVariable("id") Long id);
}
