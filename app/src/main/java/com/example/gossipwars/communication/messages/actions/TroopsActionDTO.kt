package com.example.gossipwars.communication.messages.actions

import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.entities.GameHelper
import java.io.Serializable
import java.util.*

class TroopsActionDTO(
    private val initiatorId: UUID, private val fromRegion: Int, private val toRegion: Int,
    val size: Int
) : Serializable {

    fun convertToEntity(): TroopsAction =
        TroopsAction(
            initiator = GameHelper.findPlayerByUUID(initiatorId),
            fromRegion = fromRegion, toRegion = toRegion, size = size
        )
}