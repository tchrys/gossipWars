package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.ActionEnd
import com.example.gossipwars.logic.entities.Game
import java.io.Serializable
import java.util.*

class ActionEndDTO(val playerId: UUID) : Serializable {

    fun convertToActionEnd(): ActionEnd = ActionEnd(Game.findPlayerByUUID(playerId))
}