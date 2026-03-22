package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_notification")
public class UserNotification extends BaseEntity {

    private Long userId;

    private String title;

    private String content;

    private Integer type;

    private Long businessId;

    private String businessType;

    private Integer isRead;

    private LocalDateTime readTime;
}
