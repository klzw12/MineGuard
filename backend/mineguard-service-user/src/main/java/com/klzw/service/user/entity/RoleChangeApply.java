package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_change_apply")
public class RoleChangeApply extends BaseEntity {

    private Long userId;

    private String username;

    private Long currentRoleId;

    private String currentRoleCode;

    private String currentRoleName;

    private Long applyRoleId;

    private String applyRoleCode;

    private String applyRoleName;

    private String applyReason;

    private Integer status;

    private String adminOpinion;

    private LocalDateTime handleTime;

    private Long handlerId;

    private String handlerName;
}
