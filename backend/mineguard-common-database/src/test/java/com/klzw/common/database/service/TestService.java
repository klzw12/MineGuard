package com.klzw.common.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.klzw.common.database.domain.TestEntity;

import java.util.List;

/**
 * 测试Service接口
 * 用于验证读写分离AOP功能
 */
public interface TestService extends IService<TestEntity> {

    /**
     * 插入数据（写操作，应该走主库）
     */
    boolean insertData(String name, Integer age);

    /**
     * 查询所有数据（读操作，应该走从库）
     */
    List<TestEntity> queryAll();

    /**
     * 根据ID查询（读操作，应该走从库）
     */
    TestEntity getById(Integer id);

    /**
     * 更新数据（写操作，应该走主库）
     */
    boolean updateData(Integer id, String name, Integer age);

    /**
     * 删除数据（写操作，应该走主库）
     */
    boolean deleteById(Integer id);
}
