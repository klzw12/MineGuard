package com.klzw.service.user.dto;

import lombok.Data;

@Data
public class ContactVO {
    private Long id;
    private String username;
    private String realName;
    private String roleName;
    private String avatar;
    private Integer unreadCount;
    private String lastMessage;
    private String lastMessageTime;
}
