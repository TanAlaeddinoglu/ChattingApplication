package com.chattingapp.chattingapp.chatRoom;

import com.chattingapp.chattingapp.chatRoom.dto.AddUserRequest;
import com.chattingapp.chattingapp.chatRoom.dto.GroupRoomRequest;
import com.chattingapp.chattingapp.chatRoom.dto.PrivateRoomRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @PostMapping("/private")
    public ResponseEntity<ChatRoom> createPrivateRoom(@RequestBody PrivateRoomRequest request) {

        ChatRoom room = chatRoomService.findOrCreatePrivateRoom(
                request.getUser1(),
                request.getUser2(),
                request.getCreatedBy()

        );
        return ResponseEntity.ok(room);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatRoom> createGroupRoom(@RequestBody GroupRoomRequest request) {
        ChatRoom room = chatRoomService.createRoom(
                request.getCreatedBy(),
                request.getMemberIds(),
                RoomType.GROUP,
                request.getName()

        );
        return ResponseEntity.ok(room);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatRoom>> getUserRooms(@PathVariable String userId) {
        return ResponseEntity.ok(chatRoomService.getUserRooms(userId));
    }

    //TODO: group name ile getir
    @GetMapping("/{roomName}")
    public ResponseEntity<ChatRoom> getRoomByName(@PathVariable String roomName) {
        return ResponseEntity.ok(chatRoomService.getRoomByName(roomName));
    }


    @PostMapping("/{roomId}/addUser")
    public ResponseEntity<ChatRoom> addUserToRoom(@PathVariable String roomId,
                                                  @RequestBody AddUserRequest request) {

        ChatRoom updated = chatRoomService.addUserToRoom(roomId, request.getUserId());
        return ResponseEntity.ok(updated);
    }
}
