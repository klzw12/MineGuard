package com.klzw.common.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页响应参数测试
 */
public class PageResponseTest {

    @Test
    @DisplayName("测试分页响应参数的属性设置和获取")
    public void testPageResponseProperties() {
        PageResponse<String> pageResponse = new PageResponse<>();
        
        // 测试 list
        List<String> list = new ArrayList<>();
        list.add("测试1");
        list.add("测试2");
        pageResponse.setList(list);
        assertEquals(list, pageResponse.getList());
        
        // 测试 total
        long total = 100L;
        pageResponse.setTotal(total);
        assertEquals(total, pageResponse.getTotal());
        
        // 测试 page
        int page = 1;
        pageResponse.setPage(page);
        assertEquals(page, pageResponse.getPage());
        
        // 测试 size
        int size = 10;
        pageResponse.setSize(size);
        assertEquals(size, pageResponse.getSize());
        
        // 测试 pages
        int pages = 10;
        pageResponse.setPages(pages);
        assertEquals(pages, pageResponse.getPages());
    }

    @Test
    @DisplayName("测试 of 静态方法")
    public void testPageResponseOfMethod() {
        List<String> list = new ArrayList<>();
        list.add("测试1");
        list.add("测试2");
        long total = 100L;
        int page = 1;
        int size = 10;
        
        PageResponse<String> pageResponse = PageResponse.of(total, page, size, list);
        
        assertEquals(list, pageResponse.getList());
        assertEquals(total, pageResponse.getTotal());
        assertEquals(page, pageResponse.getPage());
        assertEquals(size, pageResponse.getSize());
        assertEquals(10, pageResponse.getPages()); // 100 / 10 = 10
    }

    @Test
    @DisplayName("测试 of 静态方法（带余数）")
    public void testPageResponseOfMethodWithRemainder() {
        List<String> list = new ArrayList<>();
        list.add("测试1");
        long total = 101L;
        int page = 1;
        int size = 10;
        
        PageResponse<String> pageResponse = PageResponse.of(total, page, size, list);
        
        assertEquals(11, pageResponse.getPages()); // 101 / 10 = 10.1，向上取整为 11
    }

    @Test
    @DisplayName("测试 of 静态方法（total 为 0）")
    public void testPageResponseOfMethodWithZeroTotal() {
        List<String> list = new ArrayList<>();
        long total = 0L;
        int page = 1;
        int size = 10;
        
        PageResponse<String> pageResponse = PageResponse.of(total, page, size, list);
        
        assertEquals(0, pageResponse.getPages()); // 0 / 10 = 0
    }
}
