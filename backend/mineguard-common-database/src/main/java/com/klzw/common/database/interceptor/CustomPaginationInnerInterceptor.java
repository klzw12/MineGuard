package com.klzw.common.database.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.klzw.common.core.constant.PaginationConstants;
import com.klzw.common.database.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义分页拦截器
 * 继承自 MyBatis-Plus 的 PaginationInnerInterceptor，添加自定义逻辑
 * <p>
 * 主要功能：
 * 1. 配置数据库类型为 MySQL
 * 2. 设置最大分页限制，与 common-core 中的分页常量保持一致
 * 3. 开启分页溢出处理，确保分页参数的合理性
 * <p>
 * 可扩展点：
 * - 可以重写父类方法，添加额外的分页处理逻辑
 * - 可以根据业务需求，实现更复杂的分页策略
 */
@Slf4j
@Component
public class CustomPaginationInnerInterceptor extends PaginationInnerInterceptor {

    /**
     * 构造函数
     * @param databaseProperties 数据库配置属性，包含分页相关配置
     */
    public CustomPaginationInnerInterceptor(DatabaseProperties databaseProperties) {
        long maxLimit = databaseProperties.getMybatisPlus().getMaxLimit();
        
        // 验证配置的最大分页限制是否合理
        if (maxLimit <= 0) {
            log.warn("配置的max-limit小于等于0，使用默认值: {}", PaginationConstants.MAX_PAGE_SIZE);
            maxLimit = PaginationConstants.MAX_PAGE_SIZE;
        } else if (maxLimit > PaginationConstants.MAX_PAGE_SIZE * 10) {
            log.warn("配置的max-limit过大: {}, 建议不超过: {}", maxLimit, PaginationConstants.MAX_PAGE_SIZE);
        }
        
        // 配置数据库类型为 MySQL
        this.setDbType(com.baomidou.mybatisplus.annotation.DbType.MYSQL);
        // 设置最大分页限制
        this.setMaxLimit(maxLimit);
        // 开启分页溢出处理
        this.setOverflow(databaseProperties.getMybatisPlus().isOverflow());
        
        log.info("分页拦截器初始化完成，max-limit: {}, overflow: {}", maxLimit, databaseProperties.getMybatisPlus().isOverflow());
    }

    /**
     * 自定义分页拦截器逻辑
     * 可以在此添加额外的分页处理逻辑
     * <p>
     * 示例：
     * - 可以添加分页参数的验证
     * - 可以实现更复杂的分页策略
     * - 可以添加分页相关的日志记录
     */
    // 可以根据需要重写父类方法，添加自定义逻辑

}
