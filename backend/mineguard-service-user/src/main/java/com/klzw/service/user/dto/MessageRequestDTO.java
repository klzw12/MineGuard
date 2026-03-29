package com.klzw.service.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 消息请求 DTO
 */
@Data
public class MessageRequestDTO {
    private Long userId;
    private String title;
    private String content;
    private Integer type;
}

/**
 * 批量消息请求 DTO
 */
@Data
class BatchMessageRequestDTO {
    private List<Long> userIds;
    private String title;
    private String content;
    private Integer type;
}

/**
 * 角色消息请求 DTO
 */
@Data
class RoleMessageRequestDTO {
    private String roleCode;
    private String title;
    private String content;
    private Integer type;
}
