package com.klzw.common.core.exception;

import java.util.LinkedList;
import java.util.List;

public class ExceptionHandlerRegistry {
    private final List<ExceptionHandlerStrategy> strategies = new LinkedList<>();

    public ExceptionHandlerRegistry() {
        // 默认注册的异常处理策略
        strategies.add(new BusinessExceptionHandlerStrategy());
        strategies.add(new SystemExceptionHandlerStrategy());
        strategies.add(new DefaultExceptionHandlerStrategy());
    }

    public void register(ExceptionHandlerStrategy strategy) {
        strategies.add(0, strategy); // 新策略添加到前面，优先级更高
    }

    public ExceptionHandlerStrategy getStrategy(Throwable throwable) {
        for (ExceptionHandlerStrategy strategy : strategies) {
            if (strategy.support(throwable)) {
                return strategy;
            }
        }
        return new DefaultExceptionHandlerStrategy();
    }

    public List<ExceptionHandlerStrategy> getStrategies() {
        return strategies;
    }
}
