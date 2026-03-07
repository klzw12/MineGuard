package com.klzw.service.user.service;

import com.klzw.service.user.vo.RoleVO;

import java.util.List;

public interface RoleService {

    List<RoleVO> getAllRoles();

    RoleVO getRoleById(String id);

    RoleVO getRoleByCode(String code);
}
