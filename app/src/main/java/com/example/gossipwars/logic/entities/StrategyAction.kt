package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.StrategyActionDTO
import java.io.Serializable
import java.util.*

class StrategyAction(val initiator: Player, val target : Player, val targetRegion : Int,
                        val helpers : List<Player>, val proposalEnum: ProposalEnum) : Serializable {

    fun convertToDTO(): StrategyActionDTO = StrategyActionDTO(initiator.id, target.id, targetRegion,
                                            helpers.map { player -> player.id }, proposalEnum)
}