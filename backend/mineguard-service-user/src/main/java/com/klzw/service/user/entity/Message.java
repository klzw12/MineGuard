package com.klzw.service.user.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "messages")
public class Message {
    private String id;
    private String messageId;
    private String sender;
    private String receiver;
    private String type;
    private String content;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime readAt;
    private String businessId;
    private String businessType;
}