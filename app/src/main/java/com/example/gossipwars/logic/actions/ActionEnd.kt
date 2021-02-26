package com.example.gossipwars.logic.actions

import com.example.gossipwars.communication.messages.actions.ActionEndDTO
import com.example.gossipwars.logic.entities.Player
import java.io.Serializable

class ActionEnd(val player: Player) : Serializable {

    fun convertToDTO(): ActionEndDTO =
        ActionEndDTO(player.id)
}