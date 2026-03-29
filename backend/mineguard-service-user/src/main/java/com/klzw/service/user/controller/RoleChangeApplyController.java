package com.klzw.service.user.controller;

import com.klzw.common.web.resolver.CurrentUser;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.entity.RoleChangeApply;
import com.klzw.service.user.service.RoleChangeApplyService;
import com.klzw.service.user.vo.RoleChangeApplyVO;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色变更申请", description = "用户角色变更申请相关接口")
@RestController
@RequestMapping("/user/admin/role-change")
@RequiredArgsConstructor
@Slf4j
public class RoleChangeApplyController {

    private final RoleChangeApplyService roleChangeApplyService;

    @Operation(summary = "提交角色变更申请")
    @PostMapping("/apply")
    public Result<String> createRoleChangeApply(
            @CurrentUser Long userId,
            @Validated @RequestBody RoleChangeApply apply) {
        apply.setUserId(userId);
        String applyId = roleChangeApplyService.createRoleChangeApply(apply);
        return Result.success("申请提交成功", applyId);
    }

    @Operation(summary = "获取我的角色变更申请")
    @GetMapping("/apply/my")
    public Result<List<RoleChangeApplyVO>> getMyRoleChangeApplies(@CurrentUser Long userId) {
        List<RoleChangeApplyVO> applies = roleChangeApplyService.getRoleChangeAppliesByUserId(userId);
        return Result.success(applies);
    }
}
