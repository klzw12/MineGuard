package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_appeal")
public class UserAppeal extends BaseEntity {

    private Long userId;

    private String username;

    private String realName;

    private String phone;

    private String appealReason;

    private Integer status;

    private String adminOpinion;

    private LocalDateTime handleTime;

    private Long handlerId;

    private String handlerName;
}
