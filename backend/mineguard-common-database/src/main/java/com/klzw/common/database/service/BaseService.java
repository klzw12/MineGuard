package com.klzw.common.database.service;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 基础 Service 接口
 * 继承 MyBatis-Plus 的 IService，为所有 Service 提供基础的 CRUD 方法
 * @param <T> 实体类类型
 */
public interface BaseService<T> extends IService<T> {

    // 可以在此添加自定义的通用方法
    
}