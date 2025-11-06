package com.quokka.Chat_Service.repository;

import com.quokka.Chat_Service.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByParticipantsHash(String hash);

    @Query("{ 'participants.userId': ?0 }")
    List<Conversation> findAllByParticipantIdsContains(String userId);


    // Lấy tất cả các hội thoại có tên khớp từ khóa, mà user hiện tại đang tham gia
    @Query("{ 'participants.userId': ?0, 'participants.fullName': { $regex: ?1, $options: 'i' } }")
    List<Conversation> searchByParticipantName(String userId, String keyword);


}
