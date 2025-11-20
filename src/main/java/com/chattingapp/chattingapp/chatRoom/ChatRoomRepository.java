package com.chattingapp.chattingapp.chatRoom;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByMemberIdsContaining(String userId);

    // PRIVATE oda duplicate kontrolü → $all kullanarak custom query
    @Query("{ 'type': ?0, 'memberIds': { $all: ?1 } }")
    Optional<ChatRoom> findPrivateRoom(RoomType type, List<String> members);

    List<ChatRoom> findByNameContainingIgnoreCase(String name);

    Optional<ChatRoom> findByTypeAndMemberIds(RoomType type, List<String> memberIds);
}

