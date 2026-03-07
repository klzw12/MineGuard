package com.klzw.common.database.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlUtils 测试")
class SqlUtilsTest {

    @Test
    @DisplayName("joinInParams - 正常字符串参数")
    void joinInParams_NormalParams() {
        List<String> params = Arrays.asList("a", "b", "c");
        String result = SqlUtils.joinInParams(params);

        assertEquals("'a,b,c'", result);
    }

    @Test
    @DisplayName("joinInParams - 单个参数")
    void joinInParams_SingleParam() {
        List<String> params = Collections.singletonList("test");
        String result = SqlUtils.joinInParams(params);

        assertEquals("'test'", result);
    }

    @Test
    @DisplayName("joinInParams - 空列表")
    void joinInParams_EmptyList() {
        List<String> params = Collections.emptyList();
        String result = SqlUtils.joinInParams(params);

        assertEquals("", result);
    }

    @Test
    @DisplayName("joinInParams - null列表")
    void joinInParams_NullList() {
        String result = SqlUtils.joinInParams(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("joinInParams - 包含单引号的参数（SQL注入防护）")
    void joinInParams_WithSingleQuote() {
        List<String> params = Arrays.asList("test'or'1'='1", "normal");
        String result = SqlUtils.joinInParams(params);

        assertEquals("'test''or''1''=''1,normal'", result);
    }

    @Test
    @DisplayName("joinInParams - 多个单引号")
    void joinInParams_MultipleSingleQuotes() {
        List<String> params = Arrays.asList("it's", "don't", "can't");
        String result = SqlUtils.joinInParams(params);

        assertEquals("'it''s,don''t,can''t'", result);
    }

    @Test
    @DisplayName("joinInParams - 空字符串")
    void joinInParams_EmptyString() {
        List<String> params = Arrays.asList("", "test");
        String result = SqlUtils.joinInParams(params);

        assertEquals("',test'", result);
    }

    @Test
    @DisplayName("joinInParams - 特殊字符")
    void joinInParams_SpecialCharacters() {
        List<String> params = Arrays.asList("test@#$", "value%&*");
        String result = SqlUtils.joinInParams(params);

        assertEquals("'test@#$,value%&*'", result);
    }

    @Test
    @DisplayName("joinInNumberParams - 正常数字参数")
    void joinInNumberParams_NormalParams() {
        List<Number> params = Arrays.asList(1, 2, 3, 4, 5);
        String result = SqlUtils.joinInNumberParams(params);

        assertEquals("1,2,3,4,5", result);
    }

    @Test
    @DisplayName("joinInNumberParams - 浮点数")
    void joinInNumberParams_FloatParams() {
        List<Number> params = Arrays.asList(1.5, 2.3, 3.7);
        String result = SqlUtils.joinInNumberParams(params);

        assertEquals("1.5,2.3,3.7", result);
    }

    @Test
    @DisplayName("joinInNumberParams - 混合数字类型")
    void joinInNumberParams_MixedNumberTypes() {
        List<Number> params = Arrays.asList(1, 2L, 3.5, 4.0f);
        String result = SqlUtils.joinInNumberParams(params);

        assertEquals("1,2,3.5,4.0", result);
    }

    @Test
    @DisplayName("joinInNumberParams - 单个数字")
    void joinInNumberParams_SingleParam() {
        List<Number> params = Collections.singletonList(100);
        String result = SqlUtils.joinInNumberParams(params);

        assertEquals("100", result);
    }

    @Test
    @DisplayName("joinInNumberParams - 空列表")
    void joinInNumberParams_EmptyList() {
        List<Number> params = Collections.emptyList();
        String result = SqlUtils.joinInNumberParams(params);

        assertEquals("", result);
    }

    @Test
    @DisplayName("joinInNumberParams - null列表")
    void joinInNumberParams_NullList() {
        String result = SqlUtils.joinInNumberParams(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("joinInNumberParams - 负数")
    void joinInNumberParams_NegativeNumbers() {
        List<Number> params = Arrays.asList(-1, -2, -3);
        String result = SqlUtils.joinInNumberParams(params);

        assertEquals("-1,-2,-3", result);
    }

    @Test
    @DisplayName("joinInNumberParams - 零")
    void joinInNumberParams_Zero() {
        List<Number> params = Arrays.asList(0, 0.0);
        String result = SqlUtils.joinInNumberParams(params);

        assertEquals("0,0.0", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 正常排序")
    void buildOrderByClause_NormalOrderBy() {
        Map<String, String> orderBy = new LinkedHashMap<>();
        orderBy.put("id", "desc");
        orderBy.put("name", "asc");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY id DESC, name ASC", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 空Map")
    void buildOrderByClause_EmptyMap() {
        Map<String, String> orderBy = new HashMap<>();
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("", result);
    }

    @Test
    @DisplayName("buildOrderByClause - null Map")
    void buildOrderByClause_NullMap() {
        String result = SqlUtils.buildOrderByClause(null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 单字段排序")
    void buildOrderByClause_SingleField() {
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("id", "desc");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY id DESC", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 大写排序方向")
    void buildOrderByClause_UppercaseDirection() {
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("id", "ASC");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY id ASC", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 非法排序方向被忽略")
    void buildOrderByClause_InvalidDirection() {
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("id", "invalid");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY ", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 非法字段名被忽略")
    void buildOrderByClause_InvalidFieldName() {
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("id;DROP TABLE users--", "desc");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY ", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 包含特殊字符的字段名被忽略")
    void buildOrderByClause_SpecialCharsInFieldName() {
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("id", "desc");
        orderBy.put("name with spaces", "asc");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY id DESC", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 合法的下划线字段名")
    void buildOrderByClause_UnderscoreFieldName() {
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("user_id", "desc");
        orderBy.put("created_at", "asc");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY user_id DESC, created_at ASC", result);
    }

    @Test
    @DisplayName("buildOrderByClause - 数字字段名")
    void buildOrderByClause_NumericFieldName() {
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("field1", "desc");
        orderBy.put("field2", "asc");
        String result = SqlUtils.buildOrderByClause(orderBy);

        assertEquals("ORDER BY field1 DESC, field2 ASC", result);
    }

    @Test
    @DisplayName("buildLimitClause - 正常限制")
    void buildLimitClause_NormalLimit() {
        String result = SqlUtils.buildLimitClause(10, 20);

        assertEquals("LIMIT 10, 20", result);
    }

    @Test
    @DisplayName("buildLimitClause - 零偏移量")
    void buildLimitClause_ZeroOffset() {
        String result = SqlUtils.buildLimitClause(0, 10);

        assertEquals("LIMIT 0, 10", result);
    }

    @Test
    @DisplayName("buildLimitClause - 零限制")
    void buildLimitClause_ZeroLimit() {
        String result = SqlUtils.buildLimitClause(10, 0);

        assertEquals("LIMIT 10, 0", result);
    }

    @Test
    @DisplayName("buildLimitClause - 大数值")
    void buildLimitClause_LargeValues() {
        String result = SqlUtils.buildLimitClause(1000000, 1000000);

        assertEquals("LIMIT 1000000, 1000000", result);
    }

    @Test
    @DisplayName("buildLimitClause - 负数偏移量")
    void buildLimitClause_NegativeOffset() {
        String result = SqlUtils.buildLimitClause(-10, 20);

        assertEquals("LIMIT -10, 20", result);
    }

    @Test
    @DisplayName("escapeSql - 正常字符串")
    void escapeSql_NormalString() {
        String result = SqlUtils.escapeSql("test string");

        assertEquals("test string", result);
    }

    @Test
    @DisplayName("escapeSql - 包含单引号")
    void escapeSql_WithSingleQuote() {
        String result = SqlUtils.escapeSql("it's a test");

        assertEquals("it''s a test", result);
    }

    @Test
    @DisplayName("escapeSql - 多个单引号")
    void escapeSql_MultipleSingleQuotes() {
        String result = SqlUtils.escapeSql("'test'");

        assertEquals("''test''", result);
    }

    @Test
    @DisplayName("escapeSql - null值")
    void escapeSql_NullInput() {
        String result = SqlUtils.escapeSql(null);

        assertNull(result);
    }

    @Test
    @DisplayName("escapeSql - 空字符串")
    void escapeSql_EmptyString() {
        String result = SqlUtils.escapeSql("");

        assertEquals("", result);
    }

    @Test
    @DisplayName("escapeSql - SQL注入攻击字符串")
    void escapeSql_SqlInjectionString() {
        String result = SqlUtils.escapeSql("' OR '1'='1");

        assertEquals("'' OR ''1''=''1", result);
    }

    @Test
    @DisplayName("buildLikeParam - 前匹配模式")
    void buildLikeParam_PrefixMode() {
        String result = SqlUtils.buildLikeParam("test", 1);

        assertEquals("test%", result);
    }

    @Test
    @DisplayName("buildLikeParam - 后匹配模式")
    void buildLikeParam_SuffixMode() {
        String result = SqlUtils.buildLikeParam("test", 2);

        assertEquals("%test", result);
    }

    @Test
    @DisplayName("buildLikeParam - 前后匹配模式")
    void buildLikeParam_BothMode() {
        String result = SqlUtils.buildLikeParam("test", 3);

        assertEquals("%test%", result);
    }

    @Test
    @DisplayName("buildLikeParam - 无匹配模式")
    void buildLikeParam_NoMode() {
        String result = SqlUtils.buildLikeParam("test", 0);

        assertEquals("test", result);
    }

    @Test
    @DisplayName("buildLikeParam - null关键词")
    void buildLikeParam_NullKeyword() {
        String result = SqlUtils.buildLikeParam(null, 1);

        assertNull(result);
    }

    @Test
    @DisplayName("buildLikeParam - 空关键词")
    void buildLikeParam_EmptyKeyword() {
        String result = SqlUtils.buildLikeParam("", 1);

        assertEquals("%", result);
    }

    @Test
    @DisplayName("buildLikeParam - 包含单引号的关键词")
    void buildLikeParam_KeywordWithSingleQuote() {
        String result = SqlUtils.buildLikeParam("it's", 3);

        assertEquals("%it''s%", result);
    }

    @Test
    @DisplayName("buildLikeParam - 包含特殊字符的关键词")
    void buildLikeParam_KeywordWithSpecialChars() {
        String result = SqlUtils.buildLikeParam("test@#$", 1);

        assertEquals("test@#$%", result);
    }

    @Test
    @DisplayName("buildLikeParam - 包含百分号的关键词")
    void buildLikeParam_KeywordWithPercent() {
        String result = SqlUtils.buildLikeParam("test%", 3);

        assertEquals("%test%%", result);
    }

    @Test
    @DisplayName("buildLikeParam - 包含下划线的关键词")
    void buildLikeParam_KeywordWithUnderscore() {
        String result = SqlUtils.buildLikeParam("test_value", 3);

        assertEquals("%test_value%", result);
    }
}
