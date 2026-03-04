package com.klzw.common.websocket.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message_history")
public class MessageHistory {
    @Id
    private String id;
    
    @Indexed
    @Field("messageId")
    private String messageId;
    
    @Indexed
    @Field("messageType")
    private String messageType;
    
    @Indexed
    @Field("sender")
    private String sender;
    
    @Indexed
    @Field("receiver")
    private String receiver;
    
    @Field("timestamp")
    private Long timestamp;
    
    @Field("priority")
    private String priority;
    
    @Field("requireAck")
    private Boolean requireAck;
    
    @Field("content")
    private Object content;
    
    @Indexed
    @Field("status")
    private String status;
    
    @Field("sendTime")
    private LocalDateTime sendTime;
    
    @Field("deliverTime")
    private LocalDateTime deliverTime;
    
    @Field("readTime")
    private LocalDateTime readTime;
    
    @Field("retryCount")
    private Integer retryCount;
    
    @Field("createTime")
    private LocalDateTime createTime;
    
    @Field("expireTime")
    private LocalDateTime expireTime;
}
