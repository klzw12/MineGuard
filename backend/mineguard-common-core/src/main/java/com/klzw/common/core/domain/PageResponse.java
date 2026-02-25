package com.klzw.common.core.domain;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private long total;
    private int page;
    private int size;
    private int pages;
    private List<T> list;

    public static <T> PageResponse<T> of(long total, int page, int size, List<T> list) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) total / size));
        response.setList(list);
        return response;
    }
}
