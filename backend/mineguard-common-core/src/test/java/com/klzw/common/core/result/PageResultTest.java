package com.klzw.common.core.result;

import com.klzw.common.core.enums.ResultCodeEnum;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 分页响应结果测试类
 */
public class PageResultTest {

    @Test
    @DisplayName("测试 of 静态方法")
    public void testOfMethod() {
        long total = 100;
        int page = 1;
        int size = 10;
        List<String> list = new ArrayList<>();
        list.add("数据1");
        list.add("数据2");
        
        PageResult<String> result = PageResult.of(total, page, size, list);
        
        assertEquals(total, result.getTotal());
        assertEquals(page, result.getPage());
        assertEquals(size, result.getSize());
        assertEquals(10, result.getPages()); // 100 / 10 = 10
        assertEquals(list, result.getList());
    }

    @Test
    @DisplayName("测试 success 静态方法")
    public void testSuccessMethod() {
        long total = 100;
        int page = 1;
        int size = 10;
        List<String> list = new ArrayList<>();
        list.add("数据1");
        list.add("数据2");
        
        Result<PageResult<String>> result = PageResult.success(total, page, size, list);
        
        assertEquals(ResultCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(total, result.getData().getTotal());
        assertEquals(page, result.getData().getPage());
        assertEquals(size, result.getData().getSize());
        assertEquals(10, result.getData().getPages()); // 100 / 10 = 10
        assertEquals(list, result.getData().getList());
    }

    @Test
    @DisplayName("测试 of 静态方法（带余数）")
    public void testOfMethodWithRemainder() {
        long total = 101;
        int page = 1;
        int size = 10;
        List<String> list = new ArrayList<>();
        list.add("数据1");
        
        PageResult<String> result = PageResult.of(total, page, size, list);
        
        assertEquals(11, result.getPages()); // 101 / 10 = 10.1，向上取整为 11
    }

    @Test
    @DisplayName("测试 of 静态方法（total 为 0）")
    public void testOfMethodWithZeroTotal() {
        long total = 0;
        int page = 1;
        int size = 10;
        List<String> list = new ArrayList<>();
        
        PageResult<String> result = PageResult.of(total, page, size, list);
        
        assertEquals(1, result.getPages()); // 当 total 为 0 时，返回 1 而不是 0
    }
}
