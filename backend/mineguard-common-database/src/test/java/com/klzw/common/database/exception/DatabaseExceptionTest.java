package com.klzw.common.database.exception;

import com.klzw.common.core.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseException 测试")
class DatabaseExceptionTest {

    @Test
    @DisplayName("DatabaseException - 使用错误码和消息构造")
    void constructor_WithCodeAndMessage() {
        DatabaseException exception = new DatabaseException(1001, "数据库连接失败");

        assertEquals(1001, exception.getCode());
        assertEquals("数据库连接失败", exception.getMessage());
        assertEquals("database", exception.getModule());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("DatabaseException - 使用错误码、消息和原因构造")
    void constructor_WithCodeMessageAndCause() {
        Throwable cause = new RuntimeException("底层异常");
        DatabaseException exception = new DatabaseException(1002, "SQL执行错误", cause);

        assertEquals(1002, exception.getCode());
        assertEquals("SQL执行错误", exception.getMessage());
        assertEquals("database", exception.getModule());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("DatabaseException - 仅使用消息构造（使用默认错误码）")
    void constructor_WithMessageOnly() {
        DatabaseException exception = new DatabaseException("事务回滚失败");

        assertEquals(1000, exception.getCode());
        assertEquals("事务回滚失败", exception.getMessage());
        assertEquals("database", exception.getModule());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("DatabaseException - 使用消息和原因构造（使用默认错误码）")
    void constructor_WithMessageAndCause() {
        Throwable cause = new RuntimeException("连接超时");
        DatabaseException exception = new DatabaseException("数据库连接超时", cause);

        assertEquals(1000, exception.getCode());
        assertEquals("数据库连接超时", exception.getMessage());
        assertEquals("database", exception.getModule());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("DatabaseException - 继承自BaseException")
    void extendsBaseException() {
        DatabaseException exception = new DatabaseException("测试异常");

        assertTrue(exception instanceof BaseException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("DatabaseException - 模块标识为database")
    void moduleIdentifier() {
        DatabaseException exception = new DatabaseException("测试");

        assertEquals("database", exception.getModule());
    }

    @Test
    @DisplayName("DatabaseException - 默认错误码为1000")
    void defaultErrorCode() {
        DatabaseException exception = new DatabaseException("测试");

        assertEquals(1000, exception.getCode());
    }

    @Test
    @DisplayName("DatabaseException - 自定义错误码在有效范围内")
    void customErrorCodeInRange() {
        DatabaseException exception1 = new DatabaseException(1000, "测试1");
        DatabaseException exception2 = new DatabaseException(1099, "测试2");

        assertEquals(1000, exception1.getCode());
        assertEquals(1099, exception2.getCode());
    }

    @Test
    @DisplayName("DatabaseException - 空消息")
    void emptyMessage() {
        DatabaseException exception = new DatabaseException("");

        assertEquals("", exception.getMessage());
        assertEquals(1000, exception.getCode());
    }

    @Test
    @DisplayName("DatabaseException - null原因")
    void nullCause() {
        DatabaseException exception = new DatabaseException(1001, "测试", null);

        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("DatabaseException - 原因链")
    void causeChain() {
        Throwable rootCause = new RuntimeException("根异常");
        Throwable intermediateCause = new RuntimeException("中间异常", rootCause);
        DatabaseException exception = new DatabaseException("数据库异常", intermediateCause);

        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    @DisplayName("DatabaseException - 零错误码")
    void zeroErrorCode() {
        DatabaseException exception = new DatabaseException(0, "零错误码");

        assertEquals(0, exception.getCode());
    }

    @Test
    @DisplayName("DatabaseException - 负错误码")
    void negativeErrorCode() {
        DatabaseException exception = new DatabaseException(-1, "负错误码");

        assertEquals(-1, exception.getCode());
    }

    @Test
    @DisplayName("DatabaseException - 大错误码")
    void largeErrorCode() {
        DatabaseException exception = new DatabaseException(Integer.MAX_VALUE, "大错误码");

        assertEquals(Integer.MAX_VALUE, exception.getCode());
    }

    @Test
    @DisplayName("DatabaseException - 长消息")
    void longMessage() {
        String longMessage = "a".repeat(10000);
        DatabaseException exception = new DatabaseException(longMessage);

        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    @DisplayName("DatabaseException - 特殊字符消息")
    void specialCharactersMessage() {
        String specialMessage = "测试\n\t\r消息!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`";
        DatabaseException exception = new DatabaseException(specialMessage);

        assertEquals(specialMessage, exception.getMessage());
    }

    @Test
    @DisplayName("DatabaseException - Unicode消息")
    void unicodeMessage() {
        String unicodeMessage = "测试中文🎉🎊🎈";
        DatabaseException exception = new DatabaseException(unicodeMessage);

        assertEquals(unicodeMessage, exception.getMessage());
    }

    @Test
    @DisplayName("DatabaseException - 获取堆栈跟踪")
    void getStackTrace() {
        DatabaseException exception = new DatabaseException("测试");

        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    @DisplayName("DatabaseException - 设置堆栈跟踪")
    void setStackTrace() {
        DatabaseException exception = new DatabaseException("测试");
        StackTraceElement[] newStackTrace = new StackTraceElement[]{
                new StackTraceElement("TestClass", "testMethod", "TestClass.java", 1)
        };

        exception.setStackTrace(newStackTrace);

        assertArrayEquals(newStackTrace, exception.getStackTrace());
    }

    @Test
    @DisplayName("DatabaseException - 填充堆栈跟踪")
    void fillInStackTrace() {
        DatabaseException exception = new DatabaseException("测试");

        Throwable filledException = exception.fillInStackTrace();

        assertNotNull(filledException.getStackTrace());
        assertTrue(filledException.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("DatabaseException - toString方法")
    void toStringMethod() {
        DatabaseException exception = new DatabaseException(1001, "测试异常");

        String toString = exception.toString();

        assertTrue(toString.contains("DatabaseException"));
        assertTrue(toString.contains("测试异常"));
    }

    @Test
    @DisplayName("DatabaseException - 获取本地化消息")
    void getLocalizedMessage() {
        DatabaseException exception = new DatabaseException("测试异常");

        assertEquals("测试异常", exception.getLocalizedMessage());
    }

    @Test
    @DisplayName("DatabaseException - 多个构造函数创建的异常模块标识一致")
    void moduleConsistencyAcrossConstructors() {
        DatabaseException exception1 = new DatabaseException(1001, "测试1");
        DatabaseException exception2 = new DatabaseException(1002, "测试2", new RuntimeException());
        DatabaseException exception3 = new DatabaseException("测试3");
        DatabaseException exception4 = new DatabaseException("测试4", new RuntimeException());

        assertEquals("database", exception1.getModule());
        assertEquals("database", exception2.getModule());
        assertEquals("database", exception3.getModule());
        assertEquals("database", exception4.getModule());
    }

    @Test
    @DisplayName("DatabaseException - 错误码范围边界测试")
    void errorCodeBoundaryTest() {
        DatabaseException minException = new DatabaseException(1000, "最小错误码");
        DatabaseException maxException = new DatabaseException(1099, "最大错误码");

        assertEquals(1000, minException.getCode());
        assertEquals(1099, maxException.getCode());
    }
}
