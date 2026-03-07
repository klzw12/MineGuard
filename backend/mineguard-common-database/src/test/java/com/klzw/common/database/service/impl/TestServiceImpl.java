package com.klzw.common.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.klzw.common.database.domain.TestEntity;
import com.klzw.common.database.mapper.TestMapper;
import com.klzw.common.database.service.TestService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试Service实现类
 * 用于验证读写分离AOP功能
 */
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, TestEntity> implements TestService {

    @Override
    public boolean insertData(String name, Integer age) {
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity.setAge(age);
        return save(entity);
    }

    @Override
    public List<TestEntity> queryAll() {
        return list();
    }

    @Override
    public TestEntity getById(Integer id) {
        return super.getById(id);
    }

    @Override
    public boolean updateData(Integer id, String name, Integer age) {
        TestEntity entity = new TestEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setAge(age);
        return updateById(entity);
    }

    @Override
    public boolean deleteById(Integer id) {
        return removeById(id);
    }
}
