package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.ChatMessageDTO
import java.io.Serializable
import java.util.*

class ChatMessage(val alliance: Alliance, val content: String, val sender : Player) : Serializable {

    fun convertToDTO(): ChatMessageDTO {
        return ChatMessageDTO(alliance.id, content, sender.id)
    }
}