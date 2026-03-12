package com.klzw.common.database.util;

import com.klzw.common.database.constant.DatabaseResultCode;
import com.klzw.common.database.exception.DatabaseException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * SQL 工具类
 * 用于处理 SQL 相关的操作，如 SQL 拼接、SQL 注入防护等
 */
public class SqlUtils {

    /**
     * 拼接 IN 子句的参数
     * @param params 参数列表
     * @return 拼接后的字符串，如 "'a','b','c'"
     */
    public static String joinInParams(List<String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringJoiner joiner = new StringJoiner(",", "'", "'");
        for (String param : params) {
            // 转义单引号，防止 SQL 注入
            String escapedParam = param.replace("'", "''");
            joiner.add(escapedParam);
        }
        
        return joiner.toString();
    }

    /**
     * 拼接 IN 子句的数字参数
     * @param params 参数列表
     * @return 拼接后的字符串，如 "1,2,3"
     */
    public static String joinInNumberParams(List<Number> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringJoiner joiner = new StringJoiner(",");
        for (Number param : params) {
            joiner.add(param.toString());
        }
        
        return joiner.toString();
    }

    /**
     * 构建 ORDER BY 子句
     * @param orderBy 排序字段和方向，如 {"id": "desc", "name": "asc"}
     * @return ORDER BY 子句
     * @throws DatabaseException 当构建 ORDER BY 子句失败时抛出
     */
    public static String buildOrderByClause(Map<String, String> orderBy) {
        if (orderBy == null || orderBy.isEmpty()) {
            return "";
        }
        
        try {
            StringJoiner joiner = new StringJoiner(", ", "ORDER BY ", "");
            for (Map.Entry<String, String> entry : orderBy.entrySet()) {
                String field = entry.getKey();
                String direction = entry.getValue().toUpperCase();
                // 验证排序方向是否合法
                if ("ASC".equals(direction) || "DESC".equals(direction)) {
                    // 验证字段名是否合法，只允许字母、数字、下划线
                    if (field.matches("^[a-zA-Z0-9_]+$")) {
                        joiner.add(field + " " + direction);
                    }
                }
            }
            
            return joiner.toString();
        } catch (Exception e) {
            throw new DatabaseException(DatabaseResultCode.SQL_SYNTAX_ERROR, e);
        }
    }

    /**
     * 构建 LIMIT 子句
     * @param offset 偏移量
     * @param limit 限制数量
     * @return LIMIT 子句
     */
    public static String buildLimitClause(long offset, long limit) {
        return String.format("LIMIT %d, %d", offset, limit);
    }

    /**
     * 转义 SQL 字符串，防止 SQL 注入
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    public static String escapeSql(String input) {
        if (input == null) {
            return null;
        }
        // 转义单引号
        return input.replace("'", "''");
    }

    /**
     * 构建 LIKE 查询的参数
     * @param keyword 关键词
     * @param mode 匹配模式：1-前匹配，2-后匹配，3-前后匹配
     * @return 构建后的参数
     */
    public static String buildLikeParam(String keyword, int mode) {
        if (keyword == null) {
            return null;
        }
        
        String escapedKeyword = escapeSql(keyword);
        return switch (mode) {
            case 1 -> // 前匹配
                    escapedKeyword + "%";
            case 2 -> // 后匹配
                    "%" + escapedKeyword;
            case 3 -> // 前后匹配
                    "%" + escapedKeyword + "%";
            default -> escapedKeyword;
        };
    }

}