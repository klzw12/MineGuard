package com.klzw.common.database.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.klzw.common.core.properties.PaginationProperties;
import com.klzw.common.database.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 自定义分页拦截器
 * 继承自 MyBatis-Plus 的 PaginationInnerInterceptor，添加自定义逻辑
 */
@Slf4j
@Component
public class CustomPaginationInnerInterceptor extends PaginationInnerInterceptor {

    /**
     * 构造函数
     * @param databaseProperties   数据库配置属性
     * @param paginationProperties 分页配置属性
     */
    public CustomPaginationInnerInterceptor(
            DatabaseProperties databaseProperties,
            PaginationProperties paginationProperties) {
        
        long maxLimit = paginationProperties.getMaxPageSize();
        
        if (maxLimit <= 0) {
            log.warn("配置的max-page-size小于等于0，使用默认值: 100");
            maxLimit = 100;
        } else if (maxLimit > 1000) {
            log.warn("配置的max-page-size过大: {}, 建议不超过: 1000", maxLimit);
        }
        
        this.setDbType(com.baomidou.mybatisplus.annotation.DbType.MYSQL);
        this.setMaxLimit(maxLimit);
        this.setOverflow(databaseProperties.getMybatisPlus().isOverflow());
        
        log.info("分页拦截器初始化完成，max-limit: {}, overflow: {}", maxLimit, databaseProperties.getMybatisPlus().isOverflow());
    }
}
