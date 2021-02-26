package com.example.gossipwars.logic.actions

import com.example.gossipwars.communication.messages.actions.TroopsActionDTO
import com.example.gossipwars.logic.entities.Player
import java.io.Serializable

class TroopsAction(val initiator : Player, val fromRegion: Int, val toRegion : Int,
                   val size : Int): Serializable {

    fun convertToDTO(): TroopsActionDTO =
        TroopsActionDTO(
            initiator.id,
            fromRegion,
            toRegion,
            size
        )
}