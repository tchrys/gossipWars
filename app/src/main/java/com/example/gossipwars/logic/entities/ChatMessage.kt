package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.allianceCommunication.ChatMessageDTO
import java.io.Serializable

class ChatMessage(val alliance: Alliance, val content: String, val sender : Player) : Serializable {

    fun convertToDTO(): ChatMessageDTO =
        ChatMessageDTO(
            alliance.id,
            content,
            sender.id
        )
}