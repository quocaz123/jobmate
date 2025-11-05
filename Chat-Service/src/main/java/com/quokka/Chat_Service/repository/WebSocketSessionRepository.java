package com.quokka.Chat_Service.repository;

import com.quokka.Chat_Service.entity.WebSocketSession;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


@Repository
public interface WebSocketSessionRepository extends MongoRepository<WebSocketSession, String> {
    void deleteBySocketSessionId(String socketSessionId);

    List<WebSocketSession> findAllByUserIdIn(List<String> userIds);
}
