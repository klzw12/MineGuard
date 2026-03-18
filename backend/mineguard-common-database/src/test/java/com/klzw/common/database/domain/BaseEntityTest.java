package com.klzw.common.database.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * 基础实体类测试
 */
public class BaseEntityTest {

    @Test
    public void testBaseEntityProperties() {
        BaseEntity baseEntity = new BaseEntity();
        
        Long id = 1L;
        baseEntity.setId(id);
        assertEquals(id, baseEntity.getId());
        
        LocalDateTime createTime = LocalDateTime.now();
        baseEntity.setCreateTime(createTime);
        assertEquals(createTime, baseEntity.getCreateTime());
        
        LocalDateTime updateTime = LocalDateTime.now();
        baseEntity.setUpdateTime(updateTime);
        assertEquals(updateTime, baseEntity.getUpdateTime());
        
        Long createBy = 1L;
        baseEntity.setCreateBy(createBy);
        assertEquals(createBy, baseEntity.getCreateBy());
        
        Long updateBy = 1L;
        baseEntity.setUpdateBy(updateBy);
        assertEquals(updateBy, baseEntity.getUpdateBy());
        
        baseEntity.setDeleted(1);
        assertEquals(1, baseEntity.getDeleted());
        
        baseEntity.setDeleted(0);
        assertEquals(0, baseEntity.getDeleted());
        
        String remark = "测试备注";
        baseEntity.setRemark(remark);
        assertEquals(remark, baseEntity.getRemark());
    }

    @Test
    public void testBaseEntityDefaultValues() {
        BaseEntity baseEntity = new BaseEntity();
        
        assertNull(baseEntity.getId());
        assertNull(baseEntity.getCreateTime());
        assertNull(baseEntity.getUpdateTime());
        assertNull(baseEntity.getCreateBy());
        assertNull(baseEntity.getUpdateBy());
        assertNull(baseEntity.getDeleted());
        assertNull(baseEntity.getRemark());
    }
}
