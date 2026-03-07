package com.klzw.common.database.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 测试实体类
 * 对应test_table表
 */
@Data
@TableName("test_table")
public class TestEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer age;
}
