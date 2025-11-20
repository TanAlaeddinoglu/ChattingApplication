package com.chattingapp.chattingapp.chatRoom

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = "chat_rooms")
data class ChatRoom(
    @Id
    val uid:String = UUID.randomUUID().toString(),
    val name: String?= null,
    val type: RoomType= RoomType.PRIVATE,
    val memberIds: List<String>,
    val createdBy: String,
    val createdAt: Instant = Instant.now(),
    val lastMessagePreview: String? = null,
    val lastMessageAt: Instant? = null,

    )
enum class RoomType {
    PRIVATE,
    GROUP
}
