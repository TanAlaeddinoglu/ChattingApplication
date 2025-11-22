package com.chattingapp.chattingapp.chatRoom;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }
    public ChatRoom findOrCreatePrivateRoom(String user1, String user2, String createdBy) {

        // 1) Normalize member names
        List<String> members = new ArrayList<>(List.of(user1, user2));
        Collections.sort(members);

        String roomName = members.get(0) + "-" + members.get(1);

        // 2) Check if room already exists
        Optional<ChatRoom> existing = chatRoomRepository.findByName(roomName);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 3) Create new room
        ChatRoom newRoom = new ChatRoom(
                UUID.randomUUID().toString(),
                roomName,
                RoomType.PRIVATE,
                members,
                createdBy,
                Instant.now(),
                null,
                null
        );

        return chatRoomRepository.save(newRoom);
    }

    public ChatRoom createRoom(String createdBy,
                               List<String> memberIds,
                               RoomType type,
                               String name) {
        List<String> members = List.of(memberIds.get(0), memberIds.get(1));

        if (type.equals(RoomType.PRIVATE) && memberIds.size() == 2) {
            Optional<ChatRoom> existingRoom = chatRoomRepository.
                    findPrivateRoom(
                            RoomType.PRIVATE, members);
            if (existingRoom.isPresent()) {
                return existingRoom.get();
            }
        }
        if (type.equals(RoomType.GROUP) && (name == null || name.isBlank())) {
            throw new IllegalArgumentException("Group chat must have a name.");
        }
        ChatRoom room = new ChatRoom(
                UUID.randomUUID().toString(),
                name, type, memberIds, createdBy, Instant.now(), null, null
        );
        return chatRoomRepository.save(room);
    }

    public List<ChatRoom> getUserRooms(String userId) {
        return chatRoomRepository.findByMemberIdsContaining(userId);
    }

    public ChatRoom getRoomById(String roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room with id " + roomId + " not found."));
    }

    public ChatRoom getRoomByName(String roomName) {
        return chatRoomRepository.findByName(roomName).orElseThrow(() -> new RuntimeException("Room with name or id" + roomName + "not found"));
    }

    public ChatRoom addUserToRoom(String roomId, String newUserId) {
        ChatRoom room = getRoomById(roomId);
        if (room.getType().equals(RoomType.PRIVATE)) {
            throw new IllegalArgumentException("Cannot add more users to a PRIVATE chat room.");
        }
        if (room.getMemberIds().contains(newUserId)) {
            return room;
        }
        List<String> updatedMembers = new ArrayList<>(room.getMemberIds());
        updatedMembers.add(newUserId);

        ChatRoom updated = new ChatRoom(
                room.getUid(),
                room.getName(), room.getType(), updatedMembers, room.getCreatedBy(),
                room.getCreatedAt(), room.getLastMessagePreview(), room.getLastMessageAt()
        );
        return chatRoomRepository.save(updated);

    }

    public void updateLastMessage(String roomId, String lastMessage) {
        ChatRoom room = getRoomById(roomId);

        ChatRoom updated = new ChatRoom(
                room.getUid(),
                room.getName(),
                room.getType(),
                room.getMemberIds(),
                room.getCreatedBy(),
                room.getCreatedAt(),
                lastMessage,
                Instant.now()
        );
        chatRoomRepository.save(updated);

    }

}
