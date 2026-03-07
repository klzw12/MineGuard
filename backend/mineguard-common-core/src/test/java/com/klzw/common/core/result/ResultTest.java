package com.klzw.common.core.result;

import com.klzw.common.core.enums.ResultCodeEnum;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 统一响应结果测试类
 */
public class ResultTest {

    @Test
    @DisplayName("测试成功响应")
    public void testSuccess() {
        Result<?> result = Result.success();
        assertEquals(ResultCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试带数据的成功响应")
    public void testSuccessWithData() {
        String data = "测试数据";
        Result<String> result = Result.success(data);
        assertEquals(ResultCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("测试带自定义消息和数据的成功响应")
    public void testSuccessWithMessageAndData() {
        String message = "操作成功";
        String data = "测试数据";
        Result<String> result = Result.success(message, data);
        assertEquals(ResultCodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("测试失败响应")
    public void testFail() {
        Result<?> result = Result.fail();
        assertEquals(ResultCodeEnum.FAIL.getCode(), result.getCode());
        assertEquals("操作失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试带自定义消息的失败响应")
    public void testFailWithMessage() {
        String message = "操作失败";
        Result<?> result = Result.fail(message);
        assertEquals(ResultCodeEnum.FAIL.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试带错误码和自定义消息的失败响应")
    public void testFailWithCodeAndMessage() {
        int code = 400;
        String message = "参数错误";
        Result<?> result = Result.fail(code, message);
        assertEquals(code, result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试系统错误响应")
    public void testError() {
        Result<?> result = Result.error();
        assertEquals(ResultCodeEnum.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals("系统内部错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试带自定义消息的系统错误响应")
    public void testErrorWithMessage() {
        String message = "系统错误";
        Result<?> result = Result.error(message);
        assertEquals(ResultCodeEnum.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals(message, result.getMessage());
        assertNull(result.getData());
    }
}
