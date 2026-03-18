package com.klzw.service.trip.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.result.PageResult;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.dto.RouteDTO;
import com.klzw.service.trip.entity.Route;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.RouteMapper;
import com.klzw.service.trip.service.RouteService;
import com.klzw.service.trip.vo.RouteVO;
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
public class RouteServiceImpl implements RouteService {

    private final RouteMapper routeMapper;

    @Override
    public PageResult<RouteVO> page(PageRequest pageRequest) {
        Page<Route> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Route::getCreateTime);
        
        Page<Route> result = routeMapper.selectPage(page, wrapper);
        
        List<RouteVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), pageRequest.getPage(), pageRequest.getSize(), voList);
    }

    @Override
    public RouteVO getById(Long id) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new TripException(TripResultCode.ROUTE_NOT_FOUND);
        }
        return convertToVO(route);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RouteDTO dto) {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getRouteName, dto.getRouteName());
        if (routeMapper.selectCount(wrapper) > 0) {
            throw new TripException(TripResultCode.ROUTE_NAME_EXISTS);
        }
        
        Route route = new Route();
        BeanUtils.copyProperties(dto, route);
        route.setStatus(1);
        
        routeMapper.insert(route);
        log.info("创建路线成功，路线ID：{}，路线名称：{}", route.getId(), route.getRouteName());
        
        return route.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RouteDTO dto) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new TripException(TripResultCode.ROUTE_NOT_FOUND);
        }
        
        if (StrUtil.isNotBlank(dto.getRouteName()) && !dto.getRouteName().equals(route.getRouteName())) {
            LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Route::getRouteName, dto.getRouteName())
                   .ne(Route::getId, id);
            if (routeMapper.selectCount(wrapper) > 0) {
                throw new TripException(TripResultCode.ROUTE_NAME_EXISTS);
            }
        }
        
        BeanUtils.copyProperties(dto, route);
        route.setId(id);
        
        routeMapper.updateById(route);
        log.info("更新路线成功，路线ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new TripException(TripResultCode.ROUTE_NOT_FOUND);
        }
        
        routeMapper.deleteById(id);
        log.info("删除路线成功，路线ID：{}", id);
    }

    @Override
    public List<RouteVO> listAll() {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getStatus, 1)
               .orderByDesc(Route::getCreateTime);
        
        List<Route> routes = routeMapper.selectList(wrapper);
        return routes.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new TripException(TripResultCode.ROUTE_NOT_FOUND);
        }
        
        route.setStatus(1);
        routeMapper.updateById(route);
        log.info("启用路线成功，路线ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new TripException(TripResultCode.ROUTE_NOT_FOUND);
        }
        
        route.setStatus(2);
        routeMapper.updateById(route);
        log.info("禁用路线成功，路线ID：{}", id);
    }

    private RouteVO convertToVO(Route route) {
        RouteVO vo = new RouteVO();
        BeanUtils.copyProperties(route, vo);
        return vo;
    }
}
