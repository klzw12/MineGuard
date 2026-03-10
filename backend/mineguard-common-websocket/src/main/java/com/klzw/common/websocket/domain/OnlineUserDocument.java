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
@Document(collection = "online_user")
public class OnlineUserDocument {
    @Id
    private String id;
    
    @Indexed(unique = true)
    @Field("userId")
    private String userId;
    
    @Field("username")
    private String username;
    
    @Field("role")
    private String role;
    
    @Field("sessionId")
    private String sessionId;
    
    @Field("clientIp")
    private String clientIp;
    
    @Field("connectTime")
    private LocalDateTime connectTime;
    
    @Indexed
    @Field("lastActiveTime")
    private LocalDateTime lastActiveTime;
}
