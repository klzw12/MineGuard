package com.klzw.common.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 元数据自动填充处理器
 * 用于自动填充实体类中的公共字段，如创建时间、更新时间等
 * <p>
 * 支持的自动填充字段：
 * - createTime: 创建时间
 * - updateTime: 更新时间  
 * - version: 版本号（乐观锁）
 * - delFlag: 逻辑删除标记
 */
@Component
public class EntityMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        
        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        // 填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        // 填充版本号
        this.strictInsertFill(metaObject, "version", Integer.class, 1);
        // 填充逻辑删除标记
        this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);
    }

    /**
     * 更新操作时自动填充
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 填充版本号（乐观锁）
        Object version = metaObject.getValue("version");
        if (version != null) {
            this.strictUpdateFill(metaObject, "version", Integer.class, ((Integer) version) + 1);
        }
    }
}