package com.klzw.service.user.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private String messageId;
    private String sender;
    private String receiver;
    private String type;
    private String content;
    private String priority;
    private String businessId;
    private String businessType;
}