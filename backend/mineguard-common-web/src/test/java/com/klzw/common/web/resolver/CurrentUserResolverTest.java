package com.klzw.common.web.resolver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CurrentUserResolver 测试类
 */
@SpringJUnitConfig
@DisplayName("CurrentUserResolver 测试")
public class CurrentUserResolverTest {
    
    @Test
    @DisplayName("测试 CurrentUserResolver 初始化")
    public void testCurrentUserResolverInitialization() {
        CurrentUserResolver currentUserResolver = new CurrentUserResolver();
        assertNotNull(currentUserResolver);
    }
    
}
