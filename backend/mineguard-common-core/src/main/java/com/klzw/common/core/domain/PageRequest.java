package com.klzw.common.core.domain;

import com.klzw.common.core.constant.PaginationConstants;
import lombok.Data;

/**
 * 分页请求
 */
@Data
public class PageRequest {
    private Integer page = PaginationConstants.DEFAULT_PAGE;
    private Integer size = PaginationConstants.DEFAULT_PAGE_SIZE;
    private String sortField;
    private String sortOrder = PaginationConstants.SORT_ASC;


    public void setPage(Integer page) {
        if (page != null && page > 0) {
            this.page = page;
        }
    }

    public void setSize(Integer size) {
        if (size != null && size > 0) {
            this.size = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
        }
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}
