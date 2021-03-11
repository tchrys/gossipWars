package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.allianceCommunication.ChatMessageDTO
import java.io.Serializable
import java.sql.Time
import java.util.*

class ChatMessage(val alliance: Alliance, val content: String, val sender : Player,
                  val messageDate: Calendar = Calendar.getInstance()) : Serializable {

    fun convertToDTO(): ChatMessageDTO =
        ChatMessageDTO(
            alliance.id,
            content,
            sender.id,
            messageDate
        )
}