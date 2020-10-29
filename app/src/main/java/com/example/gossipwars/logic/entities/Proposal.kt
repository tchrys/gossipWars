package com.example.gossipwars.logic.entities

abstract class Proposal(open val alliance: Alliance, open val target : Player,
                        open val initiator : Player,
                        open var votes : MutableMap<Player, Boolean> = mutableMapOf(),
                        open val proposalEnum: ProposalEnum) {

    abstract fun allPlayersVoted() : Boolean
    abstract fun voteResult() : Boolean

}