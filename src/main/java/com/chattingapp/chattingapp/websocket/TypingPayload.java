package com.chattingapp.chattingapp.websocket;

public class TypingPayload {
    private String roomId;
    private String userId;

    public String getRoomId() {
        return roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
