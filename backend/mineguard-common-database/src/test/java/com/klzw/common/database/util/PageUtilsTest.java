package com.klzw.common.database.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.properties.PaginationProperties;
import com.klzw.common.core.result.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageUtils 测试")
class PageUtilsTest {

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
    @DisplayName("toMyBatisPlusPage - 正常分页请求")
    void toMyBatisPlusPage_NormalPageRequest() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(2);
        pageRequest.setSize(20);
        pageRequest.setSortField("id");
        pageRequest.setSortOrder("desc");

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertEquals(2L, result.getCurrent());
        assertEquals(20L, result.getSize());
        assertEquals(1, result.orders().size());
        assertEquals("id", result.orders().get(0).getColumn());
        assertFalse(result.orders().get(0).isAsc());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 空分页请求使用默认值")
    void toMyBatisPlusPage_NullPageRequest() {
        Page<String> result = PageUtils.toMyBatisPlusPage(null, paginationProperties);

        assertNotNull(result);
        assertEquals((long) paginationProperties.getDefaultPage(), result.getCurrent());
        assertEquals((long) paginationProperties.getDefaultPageSize(), result.getSize());
        assertTrue(result.orders().isEmpty());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 升序排序")
    void toMyBatisPlusPage_AscendingOrder() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setSortField("name");
        pageRequest.setSortOrder("asc");

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertEquals(1, result.orders().size());
        assertEquals("name", result.orders().get(0).getColumn());
        assertTrue(result.orders().get(0).isAsc());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 无排序字段")
    void toMyBatisPlusPage_NoSortField() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertTrue(result.orders().isEmpty());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 空排序字段")
    void toMyBatisPlusPage_EmptySortField() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertTrue(result.orders().isEmpty());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 页码为空使用默认值")
    void toMyBatisPlusPage_NullPageNum() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setSize(20);

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertEquals((long) paginationProperties.getDefaultPage(), result.getCurrent());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 页大小为空使用默认值")
    void toMyBatisPlusPage_NullPageSize() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(2);

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertEquals((long) paginationProperties.getDefaultPageSize(), result.getSize());
    }

    @Test
    @DisplayName("toPageResult - 正常分页结果")
    void toPageResult_NormalIPage() {
        IPage<String> iPage = new Page<>(2, 10);
        iPage.setTotal(100L);
        iPage.setRecords(Arrays.asList("item1", "item2", "item3"));

        PageResult<String> result = PageUtils.toPageResult(iPage);

        assertNotNull(result);
        assertEquals(100L, result.getTotal());
        assertEquals(2, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(3, result.getList().size());
        assertEquals("item1", result.getList().get(0));
    }

    @Test
    @DisplayName("toPageResult - 空分页结果")
    void toPageResult_NullIPage() {
        PageResult<String> result = PageUtils.toPageResult(null);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("toPageResult - 空记录列表")
    void toPageResult_EmptyRecords() {
        IPage<String> iPage = new Page<>(1, 10);
        iPage.setTotal(0L);
        iPage.setRecords(Collections.emptyList());

        PageResult<String> result = PageUtils.toPageResult(iPage);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("toPageResult - 自定义记录列表")
    void toPageResult_CustomRecords() {
        IPage<String> iPage = new Page<>(1, 10);
        iPage.setTotal(100L);
        iPage.setRecords(Arrays.asList("original1", "original2"));

        List<String> customRecords = Arrays.asList("custom1", "custom2", "custom3");
        PageResult<String> result = PageUtils.toPageResult(iPage, customRecords);

        assertNotNull(result);
        assertEquals(100L, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(3, result.getList().size());
        assertEquals("custom1", result.getList().get(0));
    }

    @Test
    @DisplayName("toPageResult - 空IPage和自定义记录")
    void toPageResult_NullIPageWithCustomRecords() {
        List<String> customRecords = Arrays.asList("item1", "item2");
        PageResult<String> result = PageUtils.toPageResult(null, customRecords);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2, result.getList().size());
        assertEquals("item1", result.getList().get(0));
    }

    @Test
    @DisplayName("toPageResult - 空IPage和空自定义记录")
    void toPageResult_NullIPageWithNullRecords() {
        PageResult<String> result = PageUtils.toPageResult(null, null);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(10, result.getSize());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("toPageResult - 空IPage和空列表自定义记录")
    void toPageResult_NullIPageWithEmptyRecords() {
        PageResult<String> result = PageUtils.toPageResult(null, Collections.emptyList());

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 大页码测试")
    void toMyBatisPlusPage_LargePageNumber() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1000);
        pageRequest.setSize(50);

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertEquals(1000L, result.getCurrent());
        assertEquals(50L, result.getSize());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 不区分大小写的排序方向")
    void toMyBatisPlusPage_CaseInsensitiveSortOrder() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        pageRequest.setSortField("id");
        pageRequest.setSortOrder("DESC");

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest, paginationProperties);

        assertNotNull(result);
        assertEquals(1, result.orders().size());
        assertFalse(result.orders().get(0).isAsc());
    }

    @Test
    @DisplayName("toPageResult - 大总数测试")
    void toPageResult_LargeTotal() {
        IPage<String> iPage = new Page<>(1, 10);
        iPage.setTotal(Long.MAX_VALUE);
        iPage.setRecords(Collections.singletonList("item1"));

        PageResult<String> result = PageUtils.toPageResult(iPage);

        assertNotNull(result);
        assertEquals(Long.MAX_VALUE, result.getTotal());
    }

    @Test
    @DisplayName("toMyBatisPlusPage - 无配置参数版本")
    void toMyBatisPlusPage_WithoutProperties() {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(2);
        pageRequest.setSize(20);

        Page<String> result = PageUtils.toMyBatisPlusPage(pageRequest);

        assertNotNull(result);
        assertEquals(2L, result.getCurrent());
        assertEquals(20L, result.getSize());
    }
}
