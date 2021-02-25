package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Action
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.TroopsAction
import java.io.Serializable
import java.util.*

class TroopsActionDTO(val initiatorId : UUID, val fromRegion: Int, val toRegion : Int,
                      val size : Int) : Action(initiatorId), Serializable {

    fun convertToTroopsAction(): TroopsAction =
        TroopsAction(initiator = Game.findPlayerByUUID(initiatorId),
                    fromRegion = fromRegion, toRegion = toRegion, size = size)
}