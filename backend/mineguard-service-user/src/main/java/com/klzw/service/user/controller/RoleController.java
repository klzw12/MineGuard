package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.service.RoleService;
import com.klzw.service.user.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理", description = "角色相关接口")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色列表")
    @GetMapping("/list")
    public Result<List<RoleVO>> getAllRoles() {
        List<RoleVO> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(@PathVariable Long id) {
        RoleVO role = roleService.getRoleById(id);
        return Result.success(role);
    }
    
    @Operation(summary = "创建角色")
    @PostMapping
    public Result<RoleVO> createRole(@RequestBody RoleVO roleVO) {
        RoleVO createdRole = roleService.createRole(roleVO);
        return Result.success(createdRole);
    }
    
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<RoleVO> updateRole(@PathVariable Long id, @RequestBody RoleVO roleVO) {
        RoleVO updatedRole = roleService.updateRole(id, roleVO);
        return Result.success(updatedRole);
    }
    
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRole(@PathVariable Long id) {
        boolean result = roleService.deleteRole(id);
        return Result.success(result);
    }
}
