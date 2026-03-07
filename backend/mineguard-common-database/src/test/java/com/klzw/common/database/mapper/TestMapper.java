package com.klzw.common.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.common.database.domain.TestEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试Mapper接口
 */
@Mapper
public interface TestMapper extends BaseMapper<TestEntity> {
}
