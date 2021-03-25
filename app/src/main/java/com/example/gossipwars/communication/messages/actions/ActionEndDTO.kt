package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.actions.ActionEnd
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.GameHelper
import java.io.Serializable
import java.util.*

class ActionEndDTO(val playerId: UUID) : Serializable {

    fun convertToEntity(): ActionEnd =
        ActionEnd(
            GameHelper.findPlayerByUUID(playerId)
        )
}