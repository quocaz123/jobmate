package com.quokka.Chat_Service.controller;

import com.quokka.Chat_Service.dto.ApiResponse;
import com.quokka.Chat_Service.dto.request.ConversationRequest;
import com.quokka.Chat_Service.dto.response.ConversationResponse;
import com.quokka.Chat_Service.service.ConversationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("conversations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {

    ConversationService conversationService;

    @PostMapping("/create")
    public ApiResponse<ConversationResponse> createConversation(@RequestBody ConversationRequest request) {
        return ApiResponse.success(conversationService.create(request));
    }

    @GetMapping("/my-conversations")
    public ApiResponse<List<ConversationResponse>> myConversations() {
        return ApiResponse.success(conversationService.myConversations());
    }
}
