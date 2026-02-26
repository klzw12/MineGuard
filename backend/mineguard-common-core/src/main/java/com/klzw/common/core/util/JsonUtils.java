package com.klzw.common.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JSON 工具类
 * 提供常用的 JSON 序列化和反序列化方法
 */
public class JsonUtils {
    /**
     * 将对象序列化为 JSON 字符串
     * @param obj 待序列化对象
     * @param <T> 对象类型
     * @return JSON 字符串
     */
    public static <T> String toJson(T obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse);
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象
     * @param jsonStr JSON 字符串
     * @param clazz 目标类型
     * @param <T> 目标类型
     * @return 反序列化后的对象，如果jsonStr为空白则返回null
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        return StringUtils.isBlank(jsonStr) ? null : JSON.parseObject(jsonStr, clazz);
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象（支持泛型）
     * @param jsonStr JSON 字符串
     * @param typeReference 类型引用
     * @param <T> 目标类型
     * @return 反序列化后的对象，如果jsonStr为空白则返回null
     */
    public static <T> T fromJson(String jsonStr, TypeReference<T> typeReference) {
        return StringUtils.isBlank(jsonStr) ? null : JSON.parseObject(jsonStr, typeReference);
    }

    /**
     * 判断字符串是否为有效的 JSON
     * @param jsonStr 待判断字符串
     * @return 如果是有效的 JSON 则返回true，否则返回false
     */
    public static boolean isJson(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return false;
        }
        try {
            JSON.parse(jsonStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
