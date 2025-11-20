package com.chattingapp.chattingapp.chatMessage;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage sendMessage(String senderId,
                                   String roomId,
                                   String content) {
        ChatMessage message = new ChatMessage(senderId, roomId, content);
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessagesSentBy(String senderId){
        return chatMessageRepository.findBySenderId(senderId);
    }

    public List<ChatMessage>getConversation(String roomId){
        return chatMessageRepository.
                findByRoomIdOrderByTimestampAsc(roomId);
    }
    public void deleteMessage(String uid) {
        chatMessageRepository.deleteById(uid);
    }

}
