package com.klzw.common.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.klzw.common.auth.context.UserContext;
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
 * - createBy: 创建人ID
 * - updateBy: 更新人ID
 * - deleted: 逻辑删除标记
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
        Long userId = getCurrentUserId();
        
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    /**
     * 更新操作时自动填充
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserId());
    }

    /**
     * 获取当前用户ID
     * @return 当前用户ID，如果未登录则返回null
     */
    private Long getCurrentUserId() {
        try {
            return UserContext.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
