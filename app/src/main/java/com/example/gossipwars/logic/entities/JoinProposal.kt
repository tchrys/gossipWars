package com.example.gossipwars.logic.entities

class JoinProposal @JvmOverloads
    constructor(override val alliance: Alliance, override val target: Player,
                override val initiator : Player,
                override var votes : MutableMap<Player, Boolean> = mutableMapOf(),
                override val proposalEnum: ProposalEnum = ProposalEnum.JOIN) :
        Proposal(alliance, target, initiator, votes, proposalEnum) {

    override fun allPlayersVoted(): Boolean = alliance.allianceSize() == votes.size

    override fun voteResult(): Boolean {
        if (allPlayersVoted())
            return false
        val noVotes : Int = votes.size
        var yesVotes : Int = 0
        for (v in votes.values) {
            if (v) yesVotes++
        }
        return yesVotes > noVotes / 2
    }

    fun createAction() : MembersAction =
        MembersAction(initiator.id, target.id, alliance.id, ProposalEnum.JOIN)

    fun proposalAccepted() : Boolean = allPlayersVoted() && voteResult()

    fun actionTaken() {
        alliance.addPlayer(target)
        alliance.proposalsList.remove(this)
    }


}