package com.klzw.common.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 分页配置属性
 * 通过 yml 配置分页参数，避免硬编码
 * <p>
 * 配置前缀：mineguard.pagination
 */
@Data
@Component
@ConfigurationProperties(prefix = "mineguard.pagination")
public class PaginationProperties {

    /**
     * 默认页码
     */
    private int defaultPage = 1;

    /**
     * 默认每页大小
     */
    private int defaultPageSize = 10;

    /**
     * 最大每页大小
     */
    private int maxPageSize = 100;

    /**
     * 默认排序方向
     */
    private String defaultSortOrder = "asc";
}
