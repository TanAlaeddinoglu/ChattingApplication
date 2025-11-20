package com.chattingapp.chattingapp.chatMessage

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = "chat_messages")
data class ChatMessage(
    @Id
    val uid: String = UUID.randomUUID().toString(),
    val senderId: String,
    val roomId: String,
//    val recipientId: String,
    val content: String,
    val timestamp: Instant = Instant.now(),
)
{
    constructor(senderId: String, roomId: String, content: String)
            : this(
        uid = UUID.randomUUID().toString(),
        senderId = senderId,
        roomId = roomId,
        content = content,
        timestamp = Instant.now())
}



