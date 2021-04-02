package com.example.gossipwars.communication.messages.allianceCommunication

import com.example.gossipwars.logic.entities.ChatMessage
import com.example.gossipwars.logic.entities.GameHelper
import java.io.Serializable
import java.util.*

class ChatMessageDTO(
    val allianceId: UUID, val content: String, private val sender: UUID,
    private val messageDate: Calendar
) : Serializable {

    fun convertToEntity(): ChatMessage = ChatMessage(
        GameHelper.findAllianceByUUID(allianceId),
        content, GameHelper.findPlayerByUUID(sender), messageDate
    )
}