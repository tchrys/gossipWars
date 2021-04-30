package com.example.gossipwars.logic.entities

import com.example.gossipwars.communication.messages.actions.AllianceInvitationDTO
import com.example.gossipwars.logic.proposals.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class Alliance(val id: UUID): Serializable {

    lateinit var name: String
    var playersInvolved: MutableList<Player> = mutableListOf()
    lateinit var founderId: UUID;
    val messageList: ArrayList<ChatMessage> = ArrayList()
    val proposalsList : MutableList<Proposal> = mutableListOf()
    var messagesSeen = true
    var lastMessage: Calendar? = null


    companion object Factory {
        fun initAlliance(player : Player, name : String) : Alliance {
            val alliance = Alliance(UUID.randomUUID())
            alliance.addPlayer(player)
            alliance.name = name
            alliance.founderId = player.id
            return alliance
        }
    }

    fun addMessage(message : ChatMessage) {
        var idx = 0
        while (idx < messageList.size) {
            if (messageList[idx].messageDate > message.messageDate) {
                break
            }
            idx++
        }
        messageList.add(idx, message)
    }

    fun addProposal(targetPlayer: Player, initiator: Player, proposalId: UUID,
                    proposalEnum: ProposalEnum, targetRegion: Int) {
        val proposal: Proposal = when(proposalEnum) {
            ProposalEnum.JOIN -> JoinProposal(alliance = this, proposalId = proposalId, initiator = initiator,
                                                    target = targetPlayer, proposalEnum = proposalEnum)
            ProposalEnum.KICK -> KickProposal(alliance = this, proposalId = proposalId, initiator = initiator,
                                                    target = targetPlayer, proposalEnum = proposalEnum)
            else -> StrategyProposal(alliance = this, proposalId = proposalId,
                                                    initiator = initiator, target = targetPlayer,
                                                    proposalEnum = proposalEnum, targetRegion = targetRegion)
        }
        proposalsList.add(proposal)
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

    fun convertToDTO() : AllianceInvitationDTO = AllianceInvitationDTO(name, id, founderId,
                                    playersInvolved.map { player -> player.id }.toMutableList())
}