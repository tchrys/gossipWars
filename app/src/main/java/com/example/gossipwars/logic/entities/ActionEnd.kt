package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.ActionEndDTO
import java.io.Serializable
import java.util.*

class ActionEnd(val player: Player) : Serializable {

    fun convertToDTO(): ActionEndDTO = ActionEndDTO(player.id)
}