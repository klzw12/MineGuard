package com.klzw.common.web.resolver;

import com.klzw.common.auth.context.UserContext;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 当前用户参数解析器
 * 用于解析当前用户信息
 */
@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        
        Class<?> parameterType = parameter.getParameterType();
        if (parameterType == Long.class) {
            return userId;
        } else if (parameterType == String.class) {
            return username;
        }
        
        return null;
    }
}
