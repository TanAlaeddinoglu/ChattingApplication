package com.chattingapp.chattingapp.websocket;

import com.chattingapp.chattingapp.chatMessage.ChatMessageService;
import com.chattingapp.chattingapp.chatRoom.ChatRoomService;
import com.chattingapp.chattingapp.chatRoom.UserPresenceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final UserPresenceService userPresenceService;

    public ChatWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            ChatRoomService chatRoomService,
            ChatMessageService chatMessageService,
            UserPresenceService userPresenceService
    ) {
        this.simpMessagingTemplate = messagingTemplate;
        this.chatRoomService = chatRoomService;
        this.chatMessageService = chatMessageService;
        this.userPresenceService = userPresenceService;
    }

    @MessageMapping("/message/send")
    public void sendMessage(@Payload MessagePayload payload) {
        var saved = chatMessageService.sendMessage(
                payload.getSenderId(),
                payload.getRoomId(),
                payload.getContent()
        );

        chatRoomService.updateLastMessage(payload.getRoomId(),payload.getContent());

        simpMessagingTemplate.convertAndSend("/topic/room/" + payload.getRoomId(), saved);
    }

    @MessageMapping("/typing")
    public void typing(@Payload TypingPayload payload) {
        userPresenceService.setTyping(payload.getUserId());

        simpMessagingTemplate.convertAndSend("/topic/room/" + payload.getRoomId() + "/typing", payload.getUserId());
    }
}
