package com.chattingapp.chattingapp.chatRoom

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = "user_presence")
data class UserPresence(
    @Id
    val userId: String,   // Presence is tied to the user
    val status: PresenceStatus= PresenceStatus.OFFLINE,
    val lastSeen: Instant = Instant.now()

    )
enum class PresenceStatus {
    ONLINE,
    OFFLINE,
    TYPING
}
