package com.example.gossipwars.logic.actions

import com.example.gossipwars.communication.messages.actions.StrategyActionDTO
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.io.Serializable

class StrategyAction(val initiator: Player, val target : Player, val targetRegion : Int,
                     val helpers : List<Player>, val proposalEnum: ProposalEnum) : Serializable {

    fun convertToDTO(): StrategyActionDTO =
        StrategyActionDTO(initiator.id,
            target.id,
            targetRegion,
            helpers.map { player -> player.id },
            proposalEnum
        )
}