package com.klzw.common.core.util;

import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

/**
 * JSON工具类测试
 */
public class JsonUtilsTest {

    static class TestObject {
        private String name;
        private int age;
        private boolean active;

        public TestObject() {
        }

        public TestObject(String name, int age, boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    @Test
    @DisplayName("测试对象转JSON")
    public void testToJson() {
        TestObject obj = new TestObject("测试", 20, true);
        String json = JsonUtils.toJson(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"测试\""));
        assertTrue(json.contains("\"age\":20"));
        assertTrue(json.contains("\"active\":true"));
    }

    @Test
    @DisplayName("测试JSON转对象")
    public void testFromJson() {
        String json = "{\"name\":\"测试\",\"age\":20,\"active\":true}";
        TestObject obj = JsonUtils.fromJson(json, TestObject.class);
        assertNotNull(obj);
        assertEquals("测试", obj.getName());
        assertEquals(20, obj.getAge());
        assertTrue(obj.isActive());
    }

    @Test
    @DisplayName("测试JSON转复杂对象(TypeReference)")
    public void testFromJsonWithTypeReference() {
        String json = "[{\"name\":\"测试1\",\"age\":20,\"active\":true},{\"name\":\"测试2\",\"age\":21,\"active\":false}]";
        List<TestObject> list = JsonUtils.fromJson(json, new TypeReference<List<TestObject>>() {});
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("测试1", list.get(0).getName());
        assertEquals("测试2", list.get(1).getName());
    }

    @Test
    @DisplayName("测试JSON转List")
    public void testFromJsonToList() {
        String json = "[{\"name\":\"测试1\",\"age\":20,\"active\":true},{\"name\":\"测试2\",\"age\":21,\"active\":false}]";
        List<TestObject> list = JsonUtils.fromJsonToList(json, TestObject.class);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("测试1", list.get(0).getName());
        assertEquals("测试2", list.get(1).getName());
    }

    @Test
    @DisplayName("测试空值处理")
    public void testFromJsonWithNull() {
        assertNull(JsonUtils.fromJson(null, TestObject.class));
        assertNull(JsonUtils.fromJson("", TestObject.class));
        assertNull(JsonUtils.fromJson("   ", TestObject.class));
    }

    @Test
    @DisplayName("测试JSON格式验证")
    public void testIsJson() {
        assertTrue(JsonUtils.isJson("{}"));
        assertTrue(JsonUtils.isJson("[]"));
        assertTrue(JsonUtils.isJson("{\"name\":\"测试\"}"));
        assertFalse(JsonUtils.isJson(null));
        assertFalse(JsonUtils.isJson(""));
        assertFalse(JsonUtils.isJson("   "));
        assertFalse(JsonUtils.isJson("不是JSON"));
    }
}