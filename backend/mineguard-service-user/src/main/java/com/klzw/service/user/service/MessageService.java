package com.klzw.service.user.service;

import java.util.List;

public interface MessageService {

    void sendMessage(Long userId, String title, String content, Integer type);

    void sendBatchMessage(List<Long> userIds, String title, String content, Integer type);

    void sendMessageByRole(String roleCode, String title, String content, Integer type);
}
