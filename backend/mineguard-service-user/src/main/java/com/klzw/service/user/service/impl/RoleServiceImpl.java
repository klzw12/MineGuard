package com.klzw.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.service.user.entity.Role;
import com.klzw.service.user.mapper.RoleMapper;
import com.klzw.service.user.service.RoleService;
import com.klzw.service.user.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public List<RoleVO> getAllRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getCreateTime);
        List<Role> roles = roleMapper.selectList(wrapper);
        return roles.stream()
                .map(this::convertToRoleVO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleById(String id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return null;
        }
        return convertToRoleVO(role);
    }

    @Override
    public RoleVO getRoleByCode(String code) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, code);
        Role role = roleMapper.selectOne(wrapper);
        if (role == null) {
            return null;
        }
        return convertToRoleVO(role);
    }

    private RoleVO convertToRoleVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
