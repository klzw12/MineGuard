package com.klzw.common.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.pagination")
public class PaginationProperties {

    private int defaultPage = 1;

    private int defaultPageSize = 10;

    private int maxPageSize = 100;

    private String defaultSortOrder = "asc";
}
