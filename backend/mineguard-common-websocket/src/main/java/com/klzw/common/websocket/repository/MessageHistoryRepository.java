package com.klzw.common.websocket.repository;

import com.klzw.common.websocket.domain.MessageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageHistoryRepository extends MongoRepository<MessageHistory, String> {
    
    Optional<MessageHistory> findByMessageId(String messageId);
    
    List<MessageHistory> findByReceiverAndStatus(String receiver, String status);
    
    List<MessageHistory> findByReceiverAndDeliverTimeIsNull(String receiver);
    
    Page<MessageHistory> findByReceiverOrderByTimestampDesc(String receiver, Pageable pageable);
    
    Page<MessageHistory> findBySenderOrderByTimestampDesc(String sender, Pageable pageable);
    
    List<MessageHistory> findByStatusAndRetryCountLessThan(String status, int maxRetryCount);
    
    List<MessageHistory> findByExpireTimeBefore(LocalDateTime expireTime);
    
    void deleteByExpireTimeBefore(LocalDateTime expireTime);
    
    long countByReceiverAndReadTimeIsNull(String receiver);
}
