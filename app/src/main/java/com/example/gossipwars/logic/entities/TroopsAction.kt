package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.TroopsActionDTO
import java.io.Serializable
import java.util.*

class TroopsAction(val initiator : Player, val fromRegion: Int, val toRegion : Int,
                      val size : Int): Serializable {

    fun convertToDTO(): TroopsActionDTO = TroopsActionDTO(initiator.id, fromRegion, toRegion, size)
}