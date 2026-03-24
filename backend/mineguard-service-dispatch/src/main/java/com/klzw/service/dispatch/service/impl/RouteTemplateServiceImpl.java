package com.klzw.service.dispatch.service.impl;

import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.dispatch.dto.RouteTemplateDTO;
import com.klzw.service.dispatch.entity.RouteTemplate;
import com.klzw.service.dispatch.mapper.RouteTemplateMapper;
import com.klzw.service.dispatch.service.RouteTemplateService;
import com.klzw.service.dispatch.vo.RouteTemplateVO;
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
public class RouteTemplateServiceImpl implements RouteTemplateService {

    private final RouteTemplateMapper routeTemplateMapper;

    @Override
    public PageResult<RouteTemplateVO> page(PageRequest pageRequest) {
        Page<RouteTemplate> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<RouteTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(RouteTemplate::getCreateTime);

        Page<RouteTemplate> result = routeTemplateMapper.selectPage(page, wrapper);

        List<RouteTemplateVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }

    @Override
    public RouteTemplateVO getById(Long id) {
        RouteTemplate route = routeTemplateMapper.selectById(id);
        if (route == null) {
            throw new RuntimeException("路线模板不存在");
        }
        return convertToVO(route);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RouteTemplateDTO dto) {
        LambdaQueryWrapper<RouteTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteTemplate::getRouteName, dto.getRouteName());
        if (routeTemplateMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("路线名称已存在");
        }

        RouteTemplate route = new RouteTemplate();
        BeanUtils.copyProperties(dto, route);
        route.setStatus(1);

        routeTemplateMapper.insert(route);
        log.info("创建路线模板成功，ID：{}，名称：{}", route.getId(), route.getRouteName());

        return route.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RouteTemplateDTO dto) {
        RouteTemplate route = routeTemplateMapper.selectById(id);
        if (route == null) {
            throw new RuntimeException("路线模板不存在");
        }

        if (StringUtils.hasText(dto.getRouteName()) && !dto.getRouteName().equals(route.getRouteName())) {
            LambdaQueryWrapper<RouteTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RouteTemplate::getRouteName, dto.getRouteName())
                    .ne(RouteTemplate::getId, id);
            if (routeTemplateMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("路线名称已存在");
            }
        }

        BeanUtils.copyProperties(dto, route);
        routeTemplateMapper.updateById(route);
        log.info("更新路线模板成功，ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RouteTemplate route = routeTemplateMapper.selectById(id);
        if (route == null) {
            throw new RuntimeException("路线模板不存在");
        }

        routeTemplateMapper.deleteById(id);
        log.info("删除路线模板成功，ID：{}", id);
    }

    @Override
    public List<RouteTemplateVO> listAll() {
        LambdaQueryWrapper<RouteTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteTemplate::getStatus, 1)
                .orderByDesc(RouteTemplate::getCreateTime);

        List<RouteTemplate> routes = routeTemplateMapper.selectList(wrapper);
        return routes.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        RouteTemplate route = routeTemplateMapper.selectById(id);
        if (route == null) {
            throw new RuntimeException("路线模板不存在");
        }

        route.setStatus(1);
        routeTemplateMapper.updateById(route);
        log.info("启用路线模板成功，ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        RouteTemplate route = routeTemplateMapper.selectById(id);
        if (route == null) {
            throw new RuntimeException("路线模板不存在");
        }

        route.setStatus(2);
        routeTemplateMapper.updateById(route);
        log.info("禁用路线模板成功，ID：{}", id);
    }

    private RouteTemplateVO convertToVO(RouteTemplate route) {
        RouteTemplateVO vo = new RouteTemplateVO();
        BeanUtils.copyProperties(route, vo);
        return vo;
    }
}
