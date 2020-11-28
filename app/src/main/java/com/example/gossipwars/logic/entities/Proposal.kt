package com.example.gossipwars.logic.entities

import java.util.*

abstract class Proposal(open val alliance: Alliance, open val target : Player,
                        open val initiator : Player,
                        open var votes : MutableMap<Player, Boolean> = mutableMapOf(),
                        open val proposalEnum: ProposalEnum, open val proposalId: UUID) {

    abstract fun allPlayersVoted() : Boolean
    abstract fun voteResult() : Boolean

}