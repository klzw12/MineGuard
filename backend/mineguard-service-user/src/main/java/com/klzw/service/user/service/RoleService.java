package com.klzw.service.user.service;

import com.klzw.service.user.vo.RoleVO;

import java.util.List;

public interface RoleService {

    List<RoleVO> getAllRoles();

    RoleVO getRoleById(Long id);

    RoleVO getRoleByCode(String code);
    
    RoleVO createRole(RoleVO roleVO);
    
    RoleVO updateRole(Long id, RoleVO roleVO);
    
    boolean deleteRole(Long id);
}
