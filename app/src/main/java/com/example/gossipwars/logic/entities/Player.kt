package com.example.gossipwars.logic.entities

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.gossipwars.communication.messages.allianceCommunication.JoinKickProposalDTO
import com.example.gossipwars.logic.actions.Action
import com.example.gossipwars.logic.proposals.*
import java.util.*

data class Player(var username : String, val id : UUID) {
    lateinit var army : Army
    var regionsOccupied : MutableSet<Region> = mutableSetOf()
    var trustInOthers : MutableMap<UUID, Int> = mutableMapOf()
    var alliances : MutableSet<Alliance> = mutableSetOf()

    fun quitAlliance(alliance: Alliance) {
        alliances.remove(alliance)
    }

    fun joinAlliance(alliance: Alliance) {
        alliances.add(alliance)
    }

    fun winRegion(region: Region) {
        regionsOccupied.add(region)
        region.occupiedBy = this
    }

    fun loseRegion(region: Region) {
        regionsOccupied.remove(region)
        region.occupiedBy = null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun moveTroops(fromRegion : Int, toRegion : Int, size : Int) {
        if (!army.sizePerRegion.containsKey(fromRegion))
            return;
        if (size > army.sizePerRegion[fromRegion]!!)
            return
        army.sizePerRegion[fromRegion] = army.sizePerRegion[fromRegion]!! - size
        army.sizePerRegion[toRegion] = army.sizePerRegion.getOrDefault(toRegion, 0) + size
    }


    fun createAlliance(name : String) {
        Game.addAlliance(this, name)
    }

    fun findProposalsNotVoted() : MutableList<Proposal> {
        var result : MutableList<Proposal> = mutableListOf()
        for (alliance : Alliance in alliances) {
            for (proposal : Proposal in alliance.proposalsList) {
                if (!proposal.votes.containsKey(this) && proposal.initiator != this) {
                    result.add(proposal)
                }
            }
        }
        return result
    }

    fun createActionsWhereInitiator() : MutableList<Action> {
        var result : MutableList<Action> = mutableListOf()
        for (alliance in alliances) {
            for (proposal in alliance.proposalsList) {
                if (proposal.initiator == this && proposal.proposalAccepted()) {
                    when (proposal) {
                        is JoinProposal -> {
                            if (proposal.proposalAccepted())
                                result.add(proposal.createAction())
                        }
                        is KickProposal -> {
                            if (proposal.proposalAccepted()) {
                                result.add(proposal.createAction())
                            }
                        }
                        is StrategyProposal -> {
                            result.add(proposal.createAction())
                        }
                    }
                }
            }
        }
        return result
    }

    fun makeProposal(alliance: Alliance, target : Player, proposalType: ProposalEnum, targetRegion: Int) {
        var proposalMade: Proposal = when(proposalType) {
            ProposalEnum.KICK -> KickProposal(alliance = alliance, target = target,
                                                initiator = this, proposalId = UUID.randomUUID())
            ProposalEnum.JOIN -> JoinProposal(alliance = alliance, target = target,
                                                initiator = this, proposalId = UUID.randomUUID())
            ProposalEnum.ATTACK -> StrategyProposal(alliance = alliance, initiator = this,
                                                proposalEnum = proposalType, target = target,
                                                targetRegion = targetRegion, proposalId = UUID.randomUUID())
            ProposalEnum.DEFEND -> StrategyProposal(alliance = alliance, initiator = this,
                                                    proposalEnum = proposalType, target = target,
                                                    targetRegion = targetRegion, proposalId = UUID.randomUUID())
        }
        alliance.proposalsList.add(proposalMade)
        sendProposalToOtherPlayers(proposalMade)
    }

    fun sendProposalToOtherPlayers(proposal: Proposal) {
        when(proposal.proposalEnum) {
            ProposalEnum.JOIN -> Game.sendJoinKickProposalDTO((proposal as JoinProposal).convertToDTO())
            ProposalEnum.KICK -> Game.sendJoinKickProposalDTO((proposal as KickProposal).convertToDTO())
            else -> Game.sendStrategyProposal((proposal as StrategyProposal).convertToDTO())
        }
    }

    fun iAmInvolvedInThisRegion(regionId: Int): Boolean {
        alliances.forEach { alliance: Alliance ->
            alliance.proposalsList.forEach { proposal: Proposal ->
                if (ProposalEnum.DEFEND == proposal.proposalEnum ||
                            ProposalEnum.ATTACK == proposal.proposalEnum) {
                    if (proposal.votes.containsKey(this) && proposal.votes[this]!!)
                        return true
                }
            }
        }
        return false
    }
}