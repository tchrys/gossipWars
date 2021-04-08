package com.example.gossipwars.logic.entities

import com.example.gossipwars.logic.proposals.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*
import kotlin.math.max

data class Player(var username : String, val id : UUID): Serializable {
    lateinit var army : Army
    var capitalRegion: Int = -1
    var regionsOccupied : MutableSet<Region> = mutableSetOf()
    var trustInOthers : MutableMap<UUID, Int> = mutableMapOf()
    var alliances : MutableSet<Alliance> = mutableSetOf()
    var armyRequestReceived: MutableList<ArmyRequest> = mutableListOf()
    var soldiersUsedThisRound: MutableMap<Int, Int> = mutableMapOf()

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

    fun moveTroops(fromRegion : Int, toRegion : Int, size : Int) {
        if (!army.sizePerRegion.containsKey(fromRegion))
            return;
        if (size > army.sizePerRegion[fromRegion]!!)
            return
        army.sizePerRegion[fromRegion] = army.sizePerRegion[fromRegion]!! - size
        army.sizePerRegion[toRegion] = army.sizePerRegion.getOrPut(toRegion, {0})  + size
    }

    fun improveArmy(armyRequest: ArmyRequest) {
        when (armyRequest.armyOption) {
            ArmyOption.ATTACK -> army.attack += armyRequest.increase
            ArmyOption.DEFEND -> army.defense += armyRequest.increase
            ArmyOption.SIZE -> {
                if (capitalRegion != -1) {
                    army.size += armyRequest.increase
                    army.sizePerRegion[capitalRegion] =
                        army.sizePerRegion.getOrPut(capitalRegion, {0}) + armyRequest.increase
                }
            }
        }
    }

    fun getArmySizeForRegion(regionId: Int) = army.sizePerRegion.getOrElse(regionId, {0})

    fun changeArmySizeForRegion(regionId: Int, delta: Int) {
        if (army.sizePerRegion.containsKey(regionId))
            army.sizePerRegion[regionId] = max(0, army.sizePerRegion[regionId]!! + delta)
    }

    fun computeArmySize() {
        army.size = army.sizePerRegion.values.sum()
    }

    fun getArmyAttDamage(regionId: Int): Float =
                    1f * getArmySizeForRegion(regionId) * army.attack / 400

    fun getArmyDefDamage(regionId: Int): Float =
                    1f * getArmySizeForRegion(regionId) * army.defense / 400

    fun makeProposal(alliance: Alliance, target : Player, proposalType: ProposalEnum, targetRegion: Int) {
        val proposalMade: Proposal = when(proposalType) {
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

    private fun sendProposalToOtherPlayers(proposal: Proposal) {
        GlobalScope.launch {
            when(proposal.proposalEnum) {
                ProposalEnum.JOIN -> Game.sendJoinKickProposalDTO((proposal as JoinProposal).convertToDTO())
                ProposalEnum.KICK -> Game.sendJoinKickProposalDTO((proposal as KickProposal).convertToDTO())
                else -> Game.sendStrategyProposal((proposal as StrategyProposal).convertToDTO())
            }
        }
    }
}