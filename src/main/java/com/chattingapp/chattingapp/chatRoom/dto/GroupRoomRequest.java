package com.chattingapp.chattingapp.chatRoom.dto;

import java.util.List;

public class GroupRoomRequest {
    private String createdBy;
    private String name;
    private List<String> memberIds;

    public String getName() {
        return name;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public String getCreatedBy() {
        return createdBy;
    }


}
