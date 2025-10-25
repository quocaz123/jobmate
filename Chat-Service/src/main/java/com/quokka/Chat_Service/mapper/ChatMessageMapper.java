package com.quokka.Chat_Service.mapper;

import com.quokka.Chat_Service.dto.request.ChatMessageRequest;
import com.quokka.Chat_Service.dto.response.ChatMessageResponse;
import com.quokka.Chat_Service.entity.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessage toChatMessage(ChatMessageRequest request);

    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);
}
