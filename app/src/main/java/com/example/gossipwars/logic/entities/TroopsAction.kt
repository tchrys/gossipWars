package com.example.gossipwars.logic.entities

import java.util.*

class TroopsAction(val initiatorId : UUID, val fromRegion: Int, val toRegion : Int,
                   val size : Int) : Action(initiatorId) {
}