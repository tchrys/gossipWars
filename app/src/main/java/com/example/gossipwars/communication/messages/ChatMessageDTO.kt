package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.ChatMessage
import com.example.gossipwars.logic.entities.Game
import java.io.Serializable
import java.util.*

class ChatMessageDTO(val allianceId: UUID, val content: String, val sender : UUID) : Serializable {

    fun convertToChatMessage(): ChatMessage {
        return ChatMessage(Game.findAllianceByUUID(allianceId), content,
                            Game.findPlayerByUUID(sender))
    }
}