package com.chattingapp.chattingapp.chatMessage;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySenderId(String senderId);


    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

//    List<ChatMessage> findBySenderIdAndRecipientIdOrderByTimestampAsc(
//            String senderId,
//            String recipientId);
//
//    List<ChatMessage> findBySenderIdAndRecipientIdOrRecipientIdAndSenderIdOrderByTimestampAsc(
//            String senderId1, String recipientId1,
//            String senderId2, String recipientId2
//    );
}
