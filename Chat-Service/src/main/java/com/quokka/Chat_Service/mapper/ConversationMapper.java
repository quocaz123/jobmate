package com.quokka.Chat_Service.mapper;
import com.quokka.Chat_Service.dto.response.ConversationResponse;
import com.quokka.Chat_Service.entity.Conversation;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationResponse toConversationResponse(Conversation conversation);
}
