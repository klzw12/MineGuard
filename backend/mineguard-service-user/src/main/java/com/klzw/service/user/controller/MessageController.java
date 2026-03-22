package com.klzw.service.user.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.user.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public Result<Void> sendMessage(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Integer type) {
        messageService.sendMessage(userId, title, content, type);
        return Result.success();
    }

    @PostMapping("/send-batch")
    public Result<Void> sendBatchMessage(
            @RequestParam List<Long> userIds,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Integer type) {
        messageService.sendBatchMessage(userIds, title, content, type);
        return Result.success();
    }

    @PostMapping("/send-by-role")
    public Result<Void> sendMessageByRole(
            @RequestParam String roleCode,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Integer type) {
        messageService.sendMessageByRole(roleCode, title, content, type);
        return Result.success();
    }
}
