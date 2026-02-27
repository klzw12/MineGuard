package com.klzw.common.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 扩展 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，为所有 Mapper 提供基础的 CRUD 方法
 * @param <T> 实体类类型
 */
public interface EnBaseMapper<T> extends BaseMapper<T> {

    // 可以在此添加自定义的通用方法

}