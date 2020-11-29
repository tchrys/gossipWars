package com.example.gossipwars.logic.entities

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
    }

    fun moveTroops(fromRegion : Int, toRegion : Int, size : Int) {
        if (!army.sizePerRegion.containsKey(fromRegion) || !army.sizePerRegion.containsKey(toRegion))
            return;
        if (size > army.sizePerRegion[fromRegion]!!)
            return
        army.sizePerRegion[fromRegion] = army.sizePerRegion[fromRegion]!! - size
        army.sizePerRegion[toRegion] = army.sizePerRegion[toRegion]!! + size
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
                if (proposal.initiator == this) {
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

    fun makeProposal(alliance: Alliance, target : Player, proposalType: ProposalEnum) {
        when (proposalType) {
            ProposalEnum.KICK -> {
                val kickProposal = KickProposal(alliance = alliance,
                    target = target, initiator = this, proposalId = UUID.randomUUID())
                alliance.proposalsList.add(kickProposal)
            }

            ProposalEnum.JOIN -> {
                val joinProposal = JoinProposal(alliance = alliance,
                    target = target, initiator = this, proposalId = UUID.randomUUID())
                alliance.proposalsList.add(joinProposal)
            }
        }
    }
}