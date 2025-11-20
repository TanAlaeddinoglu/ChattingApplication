package com.chattingapp.chattingapp.websocket;

public class MessagePayload {
    private String roomId;
    private String senderId;
    private String content;

    public String getContent() {
        return content;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
