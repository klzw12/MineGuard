package com.klzw.service.user.controller;

import com.klzw.common.auth.context.UserContext;
import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.ContactVO;
import com.klzw.service.user.dto.MessageVO;
import com.klzw.service.user.service.MessageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/dead-letter/list")
    public Result<Page<MessageVO>> getDeadLetterMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return Result.success(messageService.getDeadLetterMessages(pageRequest));
    }
    
    @GetMapping("/dead-letter/{id}")
    public Result<MessageVO> getDeadLetterDetail(@PathVariable String id) {
        return Result.success(messageService.getDeadLetterDetail(id));
    }
    
    @DeleteMapping("/dead-letter/{id}")
    public Result<Void> deleteDeadLetterMessage(@PathVariable String id) {
        messageService.deleteDeadLetterMessage(id);
        return Result.success();
    }

    @PostMapping("/send-batch")
    public Result<Void> sendBatchMessage(@RequestBody BatchMessageRequestDTO request) {
        messageService.sendBatchMessage(request.getUserIds(), request.getTitle(), request.getContent(), request.getType());
        return Result.success();
    }

    @PostMapping("/send-by-role")
    public Result<Void> sendMessageByRole(@RequestBody RoleMessageRequestDTO request) {
        messageService.sendMessageByRole(request.getRoleCode(), request.getTitle(), request.getContent(), request.getType());
        return Result.success();
    }
    
    @GetMapping("/list")
    public Result<List<MessageVO>> getUserMessages(
            @RequestParam(required = false) Integer type) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getUserMessages(userId, type));
    }
    
    @GetMapping("/private/{contactId}")
    public Result<List<MessageVO>> getPrivateMessages(
            @PathVariable Long contactId) {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getPrivateMessages(userId, contactId));
    }
    
    @PutMapping("/read/{messageId}")
    public Result<Void> markAsRead(@PathVariable String messageId) {
        messageService.markAsRead(messageId);
        return Result.success();
    }
    
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = UserContext.getUserId();
        messageService.markAllAsRead(userId);
        return Result.success();
    }
    
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getUnreadCount(userId));
    }
    
    @GetMapping("/contacts")
    public Result<List<ContactVO>> getContactList() {
        Long userId = UserContext.getUserId();
        return Result.success(messageService.getContactList(userId));
    }
    
    @PostMapping("/private")
    public Result<Void> sendPrivateMessage(@RequestBody PrivateMessageDTO request) {
        Long senderId = UserContext.getUserId();
        messageService.sendPrivateMessage(senderId, request.getReceiverId(), request.getContent());
        return Result.success();
    }

    @Data
    static class BatchMessageRequestDTO {
        private List<Long> userIds;
        private String title;
        private String content;
        private Integer type;
    }

    
    @Data
    static class RoleMessageRequestDTO {
        private String roleCode;
        private String title;
        private String content;
        private Integer type;
    }
    
    @Data
    static class PrivateMessageDTO {
        private Long senderId;
        private Long receiverId;
        private String content;
    }
}
