package com.chattingapp.chattingapp.chatMessage;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }
    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody SendMessageRequest request) {
        ChatMessage saved = chatMessageService.sendMessage(
                request.getSenderId(),
                request.getRoomId(),
                request.getContent()
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/conversation")
    public ResponseEntity<List<ChatMessage>> getConversations(@RequestParam String roomId) {
        List<ChatMessage> messages = chatMessageService.getConversation(roomId);
        return ResponseEntity.status(HttpStatus.OK).body(messages);
    }

    @GetMapping("/sent/{senderId}")
    public ResponseEntity<List<ChatMessage>> getMessagesSentBy(@PathVariable String senderId){
        List<ChatMessage> messages = chatMessageService.getMessagesSentBy(senderId);
        return ResponseEntity.ok(messages);
    }

}
