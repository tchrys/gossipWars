package com.example.gossipwars.communication.messages.allianceCommunication

import com.example.gossipwars.logic.entities.ChatMessage
import com.example.gossipwars.logic.entities.Game
import java.io.Serializable
import java.util.*

class ChatMessageDTO(val allianceId: UUID, val content: String, val sender : UUID,
                                            val messageDate: Calendar) : Serializable {

    fun convertToEntity(): ChatMessage = ChatMessage(Game.findAllianceByUUID(allianceId),
                                                    content, Game.findPlayerByUUID(sender), messageDate)
}