package com.klzw.service.user.dto;

import lombok.Data;

@Data
public class MessageVO {
    private String id;
    private String messageId;
    private String sender;
    private String receiver;
    private String type;
    private String status;
    private String content;
    private String createTime;
    private String readTime;
    private String title;
    private Integer isRead;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
}
