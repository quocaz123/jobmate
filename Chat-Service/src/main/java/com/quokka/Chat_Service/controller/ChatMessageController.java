package com.quokka.Chat_Service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quokka.Chat_Service.dto.ApiResponse;
import com.quokka.Chat_Service.dto.request.ChatMessageRequest;
import com.quokka.Chat_Service.dto.response.ChatMessageResponse;
import com.quokka.Chat_Service.service.ChatMessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageController {
    ChatMessageService chatMessageService;

    @PostMapping("/create")
    public ApiResponse<ChatMessageResponse> create(@RequestBody ChatMessageRequest request) throws JsonProcessingException {
        return ApiResponse.success(chatMessageService.create(request));
    }

    @GetMapping
    public ApiResponse<List<ChatMessageResponse>> getMessages(@RequestParam("conversationId") String conversationId) {
        return ApiResponse.success(chatMessageService.getMessages(conversationId));
    }
}
