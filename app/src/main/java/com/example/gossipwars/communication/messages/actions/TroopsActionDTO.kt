package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.actions.Action
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.entities.GameHelper
import java.io.Serializable
import java.util.*

class TroopsActionDTO(
    val initiatorId: UUID, val fromRegion: Int, val toRegion: Int,
    val size: Int
) : Action(initiatorId), Serializable {

    fun convertToEntity(): TroopsAction =
        TroopsAction(
            initiator = GameHelper.findPlayerByUUID(initiatorId),
            fromRegion = fromRegion, toRegion = toRegion, size = size
        )
}