package com.klzw.common.core.domain;

import com.klzw.common.core.constant.BusinessConstants;
import lombok.Data;

@Data
public class PageRequest {
    private Integer page = BusinessConstants.DEFAULT_PAGE;
    private Integer size = BusinessConstants.DEFAULT_PAGE_SIZE;
    private String sortField;
    private String sortOrder = BusinessConstants.SORT_ASC;

    public void setPage(Integer page) {
        if (page != null && page > 0) {
            this.page = page;
        }
    }

    public void setSize(Integer size) {
        if (size != null && size > 0) {
            this.size = Math.min(size, BusinessConstants.MAX_PAGE_SIZE);
        }
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}
