package com.klzw.common.database.handler;

import com.klzw.common.core.exception.BaseException;
import com.klzw.common.core.result.Result;
import com.klzw.common.database.exception.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseExceptionHandlerStrategy 测试")
class DatabaseExceptionHandlerStrategyTest {

    private DatabaseExceptionHandlerStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DatabaseExceptionHandlerStrategy();
    }

    @Test
    @DisplayName("support - 支持DatabaseException")
    void support_DatabaseException() {
        DatabaseException exception = new DatabaseException(1001, "数据库连接失败");

        boolean result = strategy.support(exception);

        assertTrue(result);
    }

    @Test
    @DisplayName("support - 不支持RuntimeException")
    void support_RuntimeException() {
        RuntimeException exception = new RuntimeException("普通异常");

        boolean result = strategy.support(exception);

        assertFalse(result);
    }

    @Test
    @DisplayName("support - 不支持BaseException")
    void support_BaseException() {
        BaseException exception = new BaseException(1001, "基础异常");

        boolean result = strategy.support(exception);

        assertFalse(result);
    }

    @Test
    @DisplayName("support - 不支持null")
    void support_Null() {
        boolean result = strategy.support(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("support - 不支持其他异常类型")
    void support_OtherExceptionTypes() {
        IllegalArgumentException exception = new IllegalArgumentException("非法参数");
        NullPointerException exception2 = new NullPointerException("空指针");
        IllegalStateException exception3 = new IllegalStateException("非法状态");

        assertFalse(strategy.support(exception));
        assertFalse(strategy.support(exception2));
        assertFalse(strategy.support(exception3));
    }

    @Test
    @DisplayName("handle - 正常处理DatabaseException")
    void handle_DatabaseException() {
        DatabaseException exception = new DatabaseException(1001, "数据库连接失败");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1001, result.getCode());
        assertEquals("数据库连接失败", result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理带原因的DatabaseException")
    void handle_DatabaseExceptionWithCause() {
        Throwable cause = new RuntimeException("底层连接异常");
        DatabaseException exception = new DatabaseException(1002, "SQL执行错误", cause);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1002, result.getCode());
        assertEquals("SQL执行错误", result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理使用默认错误码的DatabaseException")
    void handle_DatabaseExceptionWithDefaultCode() {
        DatabaseException exception = new DatabaseException("事务回滚失败");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1000, result.getCode());
        assertEquals("事务回滚失败", result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理空消息的DatabaseException")
    void handle_DatabaseExceptionWithEmptyMessage() {
        DatabaseException exception = new DatabaseException("");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1000, result.getCode());
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理长消息的DatabaseException")
    void handle_DatabaseExceptionWithLongMessage() {
        String longMessage = "a".repeat(10000);
        DatabaseException exception = new DatabaseException(longMessage);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1000, result.getCode());
        assertEquals(longMessage, result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理特殊字符消息的DatabaseException")
    void handle_DatabaseExceptionWithSpecialCharacters() {
        String specialMessage = "测试\n\t\r消息!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`";
        DatabaseException exception = new DatabaseException(specialMessage);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1000, result.getCode());
        assertEquals(specialMessage, result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理Unicode消息的DatabaseException")
    void handle_DatabaseExceptionWithUnicode() {
        String unicodeMessage = "测试中文🎉🎊🎈";
        DatabaseException exception = new DatabaseException(unicodeMessage);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1000, result.getCode());
        assertEquals(unicodeMessage, result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理边界错误码的DatabaseException")
    void handle_DatabaseExceptionWithBoundaryCodes() {
        DatabaseException minException = new DatabaseException(1000, "最小错误码");
        DatabaseException maxException = new DatabaseException(1099, "最大错误码");

        Result<?> result1 = strategy.handle(minException);
        Result<?> result2 = strategy.handle(maxException);

        assertEquals(1000, result1.getCode());
        assertEquals(1099, result2.getCode());
    }

    @Test
    @DisplayName("handle - 处理零错误码的DatabaseException")
    void handle_DatabaseExceptionWithZeroCode() {
        DatabaseException exception = new DatabaseException(0, "零错误码");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    @DisplayName("handle - 处理负错误码的DatabaseException")
    void handle_DatabaseExceptionWithNegativeCode() {
        DatabaseException exception = new DatabaseException(-1, "负错误码");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(-1, result.getCode());
    }

    @Test
    @DisplayName("handle - 处理大错误码的DatabaseException")
    void handle_DatabaseExceptionWithLargeCode() {
        DatabaseException exception = new DatabaseException(Integer.MAX_VALUE, "大错误码");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE, result.getCode());
    }

    @Test
    @DisplayName("handle - 返回的Result数据为null")
    void handle_ResultDataIsNull() {
        DatabaseException exception = new DatabaseException("测试异常");

        Result<?> result = strategy.handle(exception);

        assertNull(result.getData());
    }

    @Test
    @DisplayName("handle - 多次调用返回独立Result对象")
    void handle_MultipleCallsReturnIndependentResults() {
        DatabaseException exception1 = new DatabaseException(1001, "异常1");
        DatabaseException exception2 = new DatabaseException(1002, "异常2");

        Result<?> result1 = strategy.handle(exception1);
        Result<?> result2 = strategy.handle(exception2);

        assertNotSame(result1, result2);
        assertEquals(1001, result1.getCode());
        assertEquals(1002, result2.getCode());
    }

    @Test
    @DisplayName("handle - 处理带原因链的DatabaseException")
    void handle_DatabaseExceptionWithCauseChain() {
        Throwable rootCause = new RuntimeException("根异常");
        Throwable intermediateCause = new RuntimeException("中间异常", rootCause);
        DatabaseException exception = new DatabaseException("数据库异常", intermediateCause);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1000, result.getCode());
        assertEquals("数据库异常", result.getMessage());
    }

    @Test
    @DisplayName("handle - 处理null原因的DatabaseException")
    void handle_DatabaseExceptionWithNullCause() {
        DatabaseException exception = new DatabaseException(1001, "测试异常", null);

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1001, result.getCode());
    }

    @Test
    @DisplayName("support - 支持DatabaseException的子类")
    void support_DatabaseExceptionSubclass() {
        class CustomDatabaseException extends DatabaseException {
            public CustomDatabaseException(String message) {
                super(message);
            }
        }

        CustomDatabaseException exception = new CustomDatabaseException("自定义异常");

        assertTrue(strategy.support(exception));
    }

    @Test
    @DisplayName("handle - 处理DatabaseException的子类")
    void handle_DatabaseExceptionSubclass() {
        class CustomDatabaseException extends DatabaseException {
            public CustomDatabaseException(int code, String message) {
                super(code, message);
            }
        }

        CustomDatabaseException exception = new CustomDatabaseException(1001, "自定义异常");

        Result<?> result = strategy.handle(exception);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(1001, result.getCode());
        assertEquals("自定义异常", result.getMessage());
    }

    @Test
    @DisplayName("handle - Result的success属性为false")
    void handle_ResultSuccessIsFalse() {
        DatabaseException exception = new DatabaseException("测试异常");

        Result<?> result = strategy.handle(exception);

        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("handle - Result包含完整的异常信息")
    void handle_ResultContainsExceptionInfo() {
        DatabaseException exception = new DatabaseException(1001, "数据库连接失败");

        Result<?> result = strategy.handle(exception);

        assertEquals(1001, result.getCode());
        assertEquals("数据库连接失败", result.getMessage());
        assertEquals("database", exception.getModule());
    }
}
