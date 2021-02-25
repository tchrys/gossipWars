package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.AllianceDTO
import com.example.gossipwars.communication.messages.ChatMessageDTO
import java.util.*

data class Alliance(val id: UUID) {

    lateinit var name: String
    var playersInvolved: MutableList<Player> = mutableListOf()
    lateinit var founderId: UUID;
    val messageList: MutableList<ChatMessage> = mutableListOf()
    val proposalsList : MutableList<Proposal> = mutableListOf()


    companion object Factory {
        fun initAlliance(player : Player, name : String) : Alliance {
            var alliance = Alliance(UUID.randomUUID())
            alliance.addPlayer(player)
            alliance.name = name
            var founderId = player.id
            return alliance
        }
    }

    fun addMessage(message : ChatMessage) {
        messageList.add(message)
    }

    fun addProposal(targetPlayer: Player, initiator: Player, proposalId: UUID, proposalEnum: ProposalEnum) {
        var proposal: Proposal? = null
        when(proposalEnum) {
            ProposalEnum.KICK -> {
                proposal = KickProposal(alliance = this, proposalId = proposalId, initiator = initiator,
                                            target = targetPlayer, proposalEnum = proposalEnum)
            }
            ProposalEnum.JOIN -> {
                proposal = JoinProposal(alliance = this, proposalId = proposalId, initiator = initiator,
                                            target = targetPlayer, proposalEnum = proposalEnum)
            }
        }
        if (proposal != null) {
            proposalsList.add(proposal)
        }
    }

    fun addPlayer(player : Player) {
        playersInvolved.add(player)
        player.joinAlliance(this)
    }

    fun kickPlayer(player: Player) {
        playersInvolved.remove(player)
        player.quitAlliance(this)
    }

    fun allianceSize() : Int = playersInvolved.size

    fun convertToAllianceDTO() : AllianceDTO {
        return AllianceDTO(name, id, founderId, playersInvolved.map { player -> player.id }.toMutableList())
    }

}