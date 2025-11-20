package com.chattingapp.chattingapp.websocket;

import com.chattingapp.chattingapp.chatRoom.UserPresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceWebSocketListener {
    private final UserPresenceService userPresenceService;

    public PresenceWebSocketListener(UserPresenceService userPresenceService) {
        this.userPresenceService = userPresenceService;
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = accessor.getFirstNativeHeader("userId");

        if (userId != null) {
            userPresenceService.setOnline(userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = accessor.getFirstNativeHeader("userId");
        if (userId != null) {
            userPresenceService.setOffline(userId);
        }
    }
}
