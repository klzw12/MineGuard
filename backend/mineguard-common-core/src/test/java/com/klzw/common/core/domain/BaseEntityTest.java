package com.klzw.common.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * 基础实体类测试
 */
public class BaseEntityTest {

    @Test
    public void testBaseEntityProperties() {
        // 测试场景：测试基础实体类的属性设置和获取
        BaseEntity baseEntity = new BaseEntity();
        
        // 测试 id
        Long id = 1L;
        baseEntity.setId(id);
        assertEquals(id, baseEntity.getId());
        
        // 测试 createTime
        LocalDateTime createTime = LocalDateTime.now();
        baseEntity.setCreateTime(createTime);
        assertEquals(createTime, baseEntity.getCreateTime());
        
        // 测试 updateTime
        LocalDateTime updateTime = LocalDateTime.now();
        baseEntity.setUpdateTime(updateTime);
        assertEquals(updateTime, baseEntity.getUpdateTime());
        
        // 测试 createBy
        Integer createBy = 1;
        baseEntity.setCreateBy(createBy);
        assertEquals(createBy, baseEntity.getCreateBy());
        
        // 测试 updateBy
        Integer updateBy = 1;
        baseEntity.setUpdateBy(updateBy);
        assertEquals(updateBy, baseEntity.getUpdateBy());
        
        // 测试 delFlag
        baseEntity.setDelFlag(1);
        assertEquals(1, baseEntity.getDelFlag());
        
        baseEntity.setDelFlag(0);
        assertEquals(0, baseEntity.getDelFlag());
        
        // 测试 version
        Integer version = 1;
        baseEntity.setVersion(version);
        assertEquals(version, baseEntity.getVersion());
        
        version = 2;
        baseEntity.setVersion(version);
        assertEquals(version, baseEntity.getVersion());
    }

    @Test
    public void testBaseEntityDefaultValues() {
        // 测试场景：测试基础实体类的默认值
        BaseEntity baseEntity = new BaseEntity();
        
        assertNull(baseEntity.getId());
        assertNull(baseEntity.getCreateTime());
        assertNull(baseEntity.getUpdateTime());
        assertNull(baseEntity.getCreateBy());
        assertNull(baseEntity.getUpdateBy());
        assertNull(baseEntity.getDelFlag());
        assertNull(baseEntity.getVersion());
    }
}
