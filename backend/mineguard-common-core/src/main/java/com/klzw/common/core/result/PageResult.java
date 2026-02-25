package com.klzw.common.core.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private int page;
    private int size;
    private int pages;
    private List<T> list;

    private PageResult(long total, int page, int size, int pages, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = pages;
        this.list = list;
    }

    public static <T> PageResult<T> of(long total, int page, int size, List<T> list) {
        int pages = (int) Math.ceil((double) total / size);
        return new PageResult<>(total, page, size, pages, list);
    }

    public static <T> Result<PageResult<T>> success(long total, int page, int size, List<T> list) {
        PageResult<T> pageResult = of(total, page, size, list);
        return Result.success(pageResult);
    }
}
