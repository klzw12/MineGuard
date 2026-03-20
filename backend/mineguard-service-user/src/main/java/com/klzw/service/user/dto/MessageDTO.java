package com.klzw.service.user.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class MessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String sender;
    private String receiver;
    private String type;
    private String content;
    private String priority;
    private String businessId;
    private String businessType;
}