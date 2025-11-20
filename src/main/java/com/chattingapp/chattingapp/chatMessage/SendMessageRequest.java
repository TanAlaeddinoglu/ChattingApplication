package com.chattingapp.chattingapp.chatMessage;

public class SendMessageRequest {
    private String senderId;
    private String roomId;
    private String content;

    public SendMessageRequest(){

    }
    public SendMessageRequest(String senderId, String roomId, String content){
        this.senderId = senderId;
        this.roomId = roomId;
        this.content = content;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }



}