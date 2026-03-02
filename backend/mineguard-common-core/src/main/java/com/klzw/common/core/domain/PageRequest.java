package com.klzw.common.core.domain;

import com.klzw.common.core.properties.PaginationProperties;
import lombok.Data;

/**
 * 分页请求
 */
@Data
public class PageRequest {
    private Integer page;
    private Integer size;
    private String sortField;
    private String sortOrder;

    /**
     * 默认构造函数
     */
    public PageRequest() {
    }

    /**
     * 带配置的构造函数
     * @param properties 分页配置属性
     */
    public PageRequest(PaginationProperties properties) {
        this.page = properties.getDefaultPage();
        this.size = properties.getDefaultPageSize();
        this.sortOrder = properties.getDefaultSortOrder();
    }

    public void setPage(Integer page) {
        setPage(page, null);
    }

    public void setPage(Integer page, PaginationProperties properties) {
        if (page != null && page > 0) {
            this.page = page;
        } else if (this.page == null) {
            this.page = properties != null ? properties.getDefaultPage() : 1;
        }
    }

    public void setSize(Integer size) {
        setSize(size, null);
    }

    public void setSize(Integer size, PaginationProperties properties) {
        if (size != null && size > 0) {
            int maxPageSize = properties != null ? properties.getMaxPageSize() : 100;
            this.size = Math.min(size, maxPageSize);
        } else if (this.size == null) {
            this.size = properties != null ? properties.getDefaultPageSize() : 10;
        }
    }

    public void setSortField(String sortField) {
        if (sortField != null && !sortField.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }
        this.sortField = sortField;
    }

    public void setSortOrder(String sortOrder) {
        setSortOrder(sortOrder, null);
    }

    public void setSortOrder(String sortOrder, PaginationProperties properties) {
        if (sortOrder != null && !"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            throw new IllegalArgumentException("Invalid sort order: " + sortOrder);
        }
        this.sortOrder = sortOrder != null ? sortOrder.toLowerCase() : (properties != null ? properties.getDefaultSortOrder() : "asc");
    }

    public int getOffset() {
        return (page - 1) * size;
    }

    public int getPage() {
        return page != null ? page : 1;
    }

    public int getSize() {
        return size != null ? size : 10;
    }

    public String getSortOrder() {
        return sortOrder != null ? sortOrder : "asc";
    }
}
