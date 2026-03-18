package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.dto.MessageDTO;
import com.klzw.service.user.entity.Message;
import com.klzw.service.user.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送单播消息
     * @param messageDTO 消息DTO
     * @return 操作结果
     */
    @PostMapping("/unicast")
    public Result<?> sendUnicastMessage(@RequestBody MessageDTO messageDTO) {
        messageService.sendUnicastMessage(messageDTO);
        return Result.success("消息发送成功");
    }

    /**
     * 发送广播消息给特定群体
     * @param messageDTO 消息DTO
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @PostMapping("/broadcast")
    public Result<?> sendBroadcastMessage(@RequestBody MessageDTO messageDTO, @RequestParam List<String> userIds) {
        messageService.sendBroadcastMessage(messageDTO, userIds);
        return Result.success("广播消息发送成功");
    }

    /**
     * 发送广播消息给所有在线用户
     * @param messageDTO 消息DTO
     * @return 操作结果
     */
    @PostMapping("/broadcast/all")
    public Result<?> sendBroadcastMessageToAll(@RequestBody MessageDTO messageDTO) {
        messageService.sendBroadcastMessageToAll(messageDTO);
        return Result.success("广播消息发送成功");
    }

    /**
     * 获取用户的消息列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 消息列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Message>> getUserMessages(@PathVariable String userId, 
                                               @RequestParam(defaultValue = "1") int page, 
                                               @RequestParam(defaultValue = "20") int size) {
        List<Message> messages = messageService.getUserMessages(userId, page, size);
        return Result.success(messages);
    }

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 操作结果
     */
    @PutMapping("/{messageId}/read")
    public Result<?> markMessageAsRead(@PathVariable String messageId) {
        messageService.markMessageAsRead(messageId);
        return Result.success("消息已标记为已读");
    }

    /**
     * 删除消息
     * @param messageId 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{messageId}")
    public Result<?> deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return Result.success("消息已删除");
    }

    /**
     * 获取用户未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @GetMapping("/unread/count/{userId}")
    public Result<Long> getUnreadMessageCount(@PathVariable String userId) {
        long count = messageService.getUnreadMessageCount(userId);
        return Result.success(count);
    }
}