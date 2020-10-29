package com.example.gossipwars.logic.entities

class KickProposal @JvmOverloads constructor(override val alliance: Alliance,
    override val target: Player, override val initiator : Player,
    override var votes : MutableMap<Player, Boolean> = mutableMapOf(),
    override val proposalEnum: ProposalEnum = ProposalEnum.KICK) : Proposal(alliance, target, initiator, votes, proposalEnum) {

    override fun allPlayersVoted(): Boolean = alliance.allianceSize() == votes.size + 1

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
        MembersAction(initiator.id, target.id, alliance.id, ProposalEnum.KICK)

    fun proposalAccepted() : Boolean = allPlayersVoted() && voteResult()

    fun actionTaken() {
        alliance.kickPlayer(target)
        alliance.proposalsList.remove(this)
    }


}