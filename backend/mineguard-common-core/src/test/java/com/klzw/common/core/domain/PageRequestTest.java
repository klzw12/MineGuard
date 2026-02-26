package com.klzw.common.core.domain;

import com.klzw.common.core.constant.PaginationConstants;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 分页请求参数测试
 */
public class PageRequestTest {

    @Test
    @DisplayName("测试分页请求参数的属性设置和获取")
    public void testPageRequestProperties() {
        PageRequest pageRequest = new PageRequest();
        
        // 测试 page
        Integer page = 2;
        pageRequest.setPage(page);
        assertEquals(page, pageRequest.getPage());
        
        // 测试 size
        Integer size = 20;
        pageRequest.setSize(size);
        assertEquals(size, pageRequest.getSize());
        
        // 测试 sortField
        String sortField = "createTime";
        pageRequest.setSortField(sortField);
        assertEquals(sortField, pageRequest.getSortField());
        
        // 测试 sortOrder
        String sortOrder = "desc";
        pageRequest.setSortOrder(sortOrder);
        assertEquals(sortOrder, pageRequest.getSortOrder());
    }

    @Test
    @DisplayName("测试分页请求参数的默认值")
    public void testPageRequestDefaultValues() {
        PageRequest pageRequest = new PageRequest();
        
        assertEquals(PaginationConstants.DEFAULT_PAGE, pageRequest.getPage());
        assertEquals(PaginationConstants.DEFAULT_PAGE_SIZE, pageRequest.getSize());
        assertNull(pageRequest.getSortField());
        assertEquals(PaginationConstants.SORT_ASC, pageRequest.getSortOrder());
    }

    @Test
    @DisplayName("测试设置无效的 page 值")
    public void testPageRequestSetPageWithInvalidValues() {
        PageRequest pageRequest = new PageRequest();
        
        // 测试 page 为 null
        pageRequest.setPage(null);
        assertEquals(PaginationConstants.DEFAULT_PAGE, pageRequest.getPage());
        
        // 测试 page 为负数
        pageRequest.setPage(-1);
        assertEquals(PaginationConstants.DEFAULT_PAGE, pageRequest.getPage());
        
        // 测试 page 为 0
        pageRequest.setPage(0);
        assertEquals(PaginationConstants.DEFAULT_PAGE, pageRequest.getPage());
    }

    @Test
    @DisplayName("测试设置无效的 size 值")
    public void testPageRequestSetSizeWithInvalidValues() {
        PageRequest pageRequest = new PageRequest();
        
        // 测试 size 为 null
        pageRequest.setSize(null);
        assertEquals(PaginationConstants.DEFAULT_PAGE_SIZE, pageRequest.getSize());
        
        // 测试 size 为负数
        pageRequest.setSize(-1);
        assertEquals(PaginationConstants.DEFAULT_PAGE_SIZE, pageRequest.getSize());
        
        // 测试 size 为 0
        pageRequest.setSize(0);
        assertEquals(PaginationConstants.DEFAULT_PAGE_SIZE, pageRequest.getSize());
        
        // 测试 size 超过最大值
        pageRequest.setSize(200);
        assertEquals(PaginationConstants.MAX_PAGE_SIZE, pageRequest.getSize());
    }

    @Test
    @DisplayName("测试获取偏移量")
    public void testPageRequestGetOffset() {
        PageRequest pageRequest = new PageRequest();
        
        // 测试默认值
        assertEquals(0, pageRequest.getOffset());
        
        // 测试自定义值
        pageRequest.setPage(3);
        pageRequest.setSize(15);
        assertEquals(30, pageRequest.getOffset());
    }
}
