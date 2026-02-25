package com.klzw.common.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonUtils {
    public static <T> String toJson(T obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullBooleanAsFalse);
    }

    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        return StringUtils.isBlank(jsonStr) ? null : JSON.parseObject(jsonStr, clazz);
    }

    public static <T> T fromJson(String jsonStr, TypeReference<T> typeReference) {
        return StringUtils.isBlank(jsonStr) ? null : JSON.parseObject(jsonStr, typeReference);
    }

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
