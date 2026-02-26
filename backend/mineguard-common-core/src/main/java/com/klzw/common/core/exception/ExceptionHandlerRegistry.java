package com.klzw.common.core.exception;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * 异常处理策略注册中心
 */
@Getter
public class ExceptionHandlerRegistry {
    private final List<ExceptionHandlerStrategy> strategies = new LinkedList<>();

    public ExceptionHandlerRegistry() {
        this(null);
    }

    /**
     * 支持在默认策略基础上，按模块注入额外的异常处理策略。
     * 额外策略会被插入到默认策略之前，优先级高于 Default、低于 Business/System。
     */
    public ExceptionHandlerRegistry(List<ExceptionHandlerStrategy> extraStrategies) {
        // 默认注册的异常处理策略
        strategies.add(new BusinessExceptionHandlerStrategy());
        strategies.add(new SystemExceptionHandlerStrategy());
        strategies.add(new DefaultExceptionHandlerStrategy());
        
        if (extraStrategies != null) {
            for (ExceptionHandlerStrategy strategy : extraStrategies) {
                if (strategy == null) {
                    continue;
                }
                registerModuleStrategy(strategy);
            }
        }
    }

    public void register(ExceptionHandlerStrategy strategy) {
        strategies.addFirst(strategy); // 新策略添加到前面，优先级更高
    }

    public void registerModuleStrategy(ExceptionHandlerStrategy strategy) {
        // 模块异常策略添加到默认策略之前，业务异常之后
        int index = 0;
        for (int i = 0; i < strategies.size(); i++) {
            if (strategies.get(i) instanceof DefaultExceptionHandlerStrategy) {
                index = i;
                break;
            }
        }
        strategies.add(index, strategy);
    }

    public ExceptionHandlerStrategy getStrategy(Throwable throwable) {
        for (ExceptionHandlerStrategy strategy : strategies) {
            if (strategy.support(throwable)) {
                return strategy;
            }
        }
        return new DefaultExceptionHandlerStrategy();
    }
}
