package com.klzw.service.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klzw.service.warning.entity.WarningRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WarningRuleMapper extends BaseMapper<WarningRule> {
    
    @Select("SELECT * FROM warning_rule WHERE rule_code = #{ruleCode} AND deleted = 0")
    WarningRule selectByRuleCode(String ruleCode);
}
