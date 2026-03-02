package com.klzw.common.core.domain;

import com.klzw.common.core.properties.PaginationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分页请求参数测试
 */
public class PageRequestTest {

    private PaginationProperties paginationProperties;

    @BeforeEach
    void setUp() {
        paginationProperties = new PaginationProperties();
        paginationProperties.setDefaultPage(1);
        paginationProperties.setDefaultPageSize(10);
        paginationProperties.setMaxPageSize(100);
        paginationProperties.setDefaultSortOrder("asc");
    }

    @Test
    @DisplayName("测试分页请求参数的属性设置和获取")
    public void testPageRequestProperties() {
        PageRequest pageRequest = new PageRequest();
        
        pageRequest.setPage(2);
        assertEquals(2, pageRequest.getPage());
        
        pageRequest.setSize(20);
        assertEquals(20, pageRequest.getSize());
        
        pageRequest.setSortField("createTime");
        assertEquals("createTime", pageRequest.getSortField());
        
        pageRequest.setSortOrder("desc");
        assertEquals("desc", pageRequest.getSortOrder());
    }

    @Test
    @DisplayName("测试带配置的分页请求参数默认值")
    public void testPageRequestWithPropertiesDefaultValues() {
        PageRequest pageRequest = new PageRequest(paginationProperties);
        
        assertEquals(paginationProperties.getDefaultPage(), pageRequest.getPage());
        assertEquals(paginationProperties.getDefaultPageSize(), pageRequest.getSize());
        assertNull(pageRequest.getSortField());
        assertEquals(paginationProperties.getDefaultSortOrder(), pageRequest.getSortOrder());
    }

    @Test
    @DisplayName("测试无配置的分页请求参数默认值")
    public void testPageRequestWithoutPropertiesDefaultValues() {
        PageRequest pageRequest = new PageRequest();
        
        assertEquals(1, pageRequest.getPage());
        assertEquals(10, pageRequest.getSize());
        assertEquals("asc", pageRequest.getSortOrder());
    }

    @Test
    @DisplayName("测试设置无效的 page 值")
    public void testPageRequestSetPageWithInvalidValues() {
        PageRequest pageRequest = new PageRequest(paginationProperties);
        
        pageRequest.setPage(null);
        assertEquals(paginationProperties.getDefaultPage(), pageRequest.getPage());
        
        pageRequest.setPage(-1);
        assertEquals(paginationProperties.getDefaultPage(), pageRequest.getPage());
        
        pageRequest.setPage(0);
        assertEquals(paginationProperties.getDefaultPage(), pageRequest.getPage());
    }

    @Test
    @DisplayName("测试设置无效的 size 值")
    public void testPageRequestSetSizeWithInvalidValues() {
        PageRequest pageRequest = new PageRequest(paginationProperties);
        
        pageRequest.setSize(null);
        assertEquals(paginationProperties.getDefaultPageSize(), pageRequest.getSize());
        
        pageRequest.setSize(-1);
        assertEquals(paginationProperties.getDefaultPageSize(), pageRequest.getSize());
        
        pageRequest.setSize(0);
        assertEquals(paginationProperties.getDefaultPageSize(), pageRequest.getSize());
        
        pageRequest.setSize(200);
        assertEquals(paginationProperties.getMaxPageSize(), pageRequest.getSize());
    }

    @Test
    @DisplayName("测试获取偏移量")
    public void testPageRequestGetOffset() {
        PageRequest pageRequest = new PageRequest(paginationProperties);
        
        assertEquals(0, pageRequest.getOffset());
        
        pageRequest.setPage(3);
        pageRequest.setSize(15);
        assertEquals(30, pageRequest.getOffset());
    }

    @Test
    @DisplayName("测试无效的排序字段")
    public void testInvalidSortField() {
        PageRequest pageRequest = new PageRequest();
        
        assertThrows(IllegalArgumentException.class, () -> {
            pageRequest.setSortField("invalid-field!");
        });
    }

    @Test
    @DisplayName("测试无效的排序方向")
    public void testInvalidSortOrder() {
        PageRequest pageRequest = new PageRequest();
        
        assertThrows(IllegalArgumentException.class, () -> {
            pageRequest.setSortOrder("invalid");
        });
    }

    @Test
    @DisplayName("测试排序方向大小写不敏感")
    public void testSortOrderCaseInsensitive() {
        PageRequest pageRequest = new PageRequest();
        
        pageRequest.setSortOrder("DESC");
        assertEquals("desc", pageRequest.getSortOrder());
        
        pageRequest.setSortOrder("ASC");
        assertEquals("asc", pageRequest.getSortOrder());
    }
}
