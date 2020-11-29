package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Action
import java.io.Serializable
import java.util.*

class TroopsAction(private val initiatorId : UUID, val fromRegion: Int, val toRegion : Int,
                   val size : Int) : Action(initiatorId), Serializable {
    fun getInitiatorId(): UUID = initiatorId
}