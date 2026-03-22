package com.klzw.service.warning.service.impl;

import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.warning.constant.WarningResultCode;
import com.klzw.service.warning.dto.WarningRuleDTO;
import com.klzw.service.warning.entity.WarningRule;
import com.klzw.service.warning.enums.WarningLevelEnum;
import com.klzw.service.warning.enums.WarningTypeEnum;
import com.klzw.service.warning.exception.WarningException;
import com.klzw.service.warning.mapper.WarningRuleMapper;
import com.klzw.service.warning.service.WarningRuleService;
import com.klzw.service.warning.vo.WarningRuleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarningRuleServiceImpl implements WarningRuleService {

    private final WarningRuleMapper warningRuleMapper;

    @Override
    public PageResult<WarningRuleVO> page(PageRequest pageRequest) {
        Page<WarningRule> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<WarningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WarningRule::getCreateTime);

        Page<WarningRule> result = warningRuleMapper.selectPage(page, wrapper);

        List<WarningRuleVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }

    @Override
    public WarningRuleVO getById(Long id) {
        WarningRule rule = warningRuleMapper.selectById(id);
        if (rule == null) {
            throw new WarningException(WarningResultCode.WARNING_RULE_NOT_FOUND, "预警规则不存在：" + id);
        }
        return convertToVO(rule);
    }

    @Override
    public WarningRuleVO getByCode(String ruleCode) {
        LambdaQueryWrapper<WarningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRule::getRuleCode, ruleCode);
        WarningRule rule = warningRuleMapper.selectOne(wrapper);
        return rule != null ? convertToVO(rule) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WarningRuleDTO dto) {
        LambdaQueryWrapper<WarningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRule::getRuleCode, dto.getRuleCode());
        if (warningRuleMapper.selectCount(wrapper) > 0) {
            throw new WarningException(WarningResultCode.WARNING_RULE_CODE_EXISTS);
        }

        WarningRule rule = new WarningRule();
        BeanUtils.copyProperties(dto, rule);
        rule.setStatus(1);

        warningRuleMapper.insert(rule);
        log.info("创建预警规则成功，ID：{}，名称：{}", rule.getId(), rule.getRuleName());

        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, WarningRuleDTO dto) {
        WarningRule rule = warningRuleMapper.selectById(id);
        if (rule == null) {
            throw new WarningException(WarningResultCode.WARNING_RULE_NOT_FOUND, "预警规则不存在：" + id);
        }

        if (StringUtils.hasText(dto.getRuleCode()) && !dto.getRuleCode().equals(rule.getRuleCode())) {
            LambdaQueryWrapper<WarningRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WarningRule::getRuleCode, dto.getRuleCode())
                    .ne(WarningRule::getId, id);
            if (warningRuleMapper.selectCount(wrapper) > 0) {
                throw new WarningException(WarningResultCode.WARNING_RULE_CODE_EXISTS);
            }
        }

        BeanUtils.copyProperties(dto, rule);
        warningRuleMapper.updateById(rule);
        log.info("更新预警规则成功，ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WarningRule rule = warningRuleMapper.selectById(id);
        if (rule == null) {
            throw new WarningException(WarningResultCode.WARNING_RULE_NOT_FOUND, "预警规则不存在：" + id);
        }

        warningRuleMapper.deleteById(id);
        log.info("删除预警规则成功，ID：{}", id);
    }

    @Override
    public List<WarningRuleVO> listAll() {
        LambdaQueryWrapper<WarningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRule::getStatus, 1)
                .orderByDesc(WarningRule::getCreateTime);

        List<WarningRule> rules = warningRuleMapper.selectList(wrapper);
        return rules.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        WarningRule rule = warningRuleMapper.selectById(id);
        if (rule == null) {
            throw new WarningException(WarningResultCode.WARNING_RULE_NOT_FOUND, "预警规则不存在：" + id);
        }

        rule.setStatus(1);
        warningRuleMapper.updateById(rule);
        log.info("启用预警规则成功，ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        WarningRule rule = warningRuleMapper.selectById(id);
        if (rule == null) {
            throw new WarningException(WarningResultCode.WARNING_RULE_NOT_FOUND, "预警规则不存在：" + id);
        }

        rule.setStatus(0);
        warningRuleMapper.updateById(rule);
        log.info("禁用预警规则成功，ID：{}", id);
    }

    private WarningRuleVO convertToVO(WarningRule rule) {
        WarningRuleVO vo = new WarningRuleVO();
        BeanUtils.copyProperties(rule, vo);

        WarningTypeEnum typeEnum = WarningTypeEnum.getByCode(rule.getWarningType());
        if (typeEnum != null) {
            vo.setWarningTypeName(typeEnum.getName());
        }

        WarningLevelEnum levelEnum = WarningLevelEnum.getByCode(rule.getWarningLevel());
        if (levelEnum != null) {
            vo.setWarningLevelName(levelEnum.getName());
        }

        return vo;
    }
}
