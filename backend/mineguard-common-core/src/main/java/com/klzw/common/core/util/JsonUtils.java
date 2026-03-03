package com.klzw.common.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * JSON 工具类（基于 FastJSON）
 */
@Component
public class JsonUtils {

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return JSON.toJSONString(obj);
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * JSON 字符串转复杂对象（支持泛型）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return JSON.parseObject(json, typeReference);
    }

    /**
     * JSON 字符串转 List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return JSON.parseArray(json, clazz);
    }

    /**
     * JSON 字符串转 Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        return JSON.parseObject(json);
    }

    /**
     * 验证是否为有效的 JSON 字符串
     */
    public static boolean isJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                JSON.parseObject(json);
                return true;
            }
            if (json.startsWith("[") && json.endsWith("]")) {
                JSON.parseArray(json);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}