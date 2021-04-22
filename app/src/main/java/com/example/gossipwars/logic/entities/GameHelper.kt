package com.example.gossipwars.logic.entities

import android.graphics.Color
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.info.RegionPlayerInfo
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.proposals.ArmyRequest
import com.example.gossipwars.logic.proposals.Proposal
import com.example.gossipwars.logic.proposals.ProposalEnum
import java.util.*

object GameHelper {
    fun findPlayerByUUID(lookupId: UUID): Player =
        Game.players.value?.find { player -> player.id == lookupId }!!

    fun findPlayerByUsername(username: String): Player? =
        Game.players.value?.find { player -> player.username == username }

    fun findAllianceByUUID(lookupId: UUID): Alliance =
        Game.alliances.find { alliance -> alliance.id == lookupId }!!

    fun findAllianceByName(allianceName: String): Alliance? =
        Game.alliances.find { alliance -> alliance.name == allianceName }

    fun findPlayersInsideAlliance(allianceName: String): MutableList<Player>? =
        findAllianceByName(allianceName)?.playersInvolved?.filter { it.id != Game.myId }
            ?.toMutableList()

    fun playerBelongToAlliance(alliance: Alliance, player: Player): Boolean =
        alliance.playersInvolved.any { playerInv -> playerInv.id == player.id }

    fun findAlliancesForPlayer(playerId: UUID): MutableList<Alliance>? =
        Game.alliances.filter { alliance ->
            alliance.playersInvolved.any { player -> player.id == playerId }
        }.toMutableList()

    fun findAllProposals(): List<Proposal>? = findAlliancesForPlayer(Game.myId)?.flatMap { alliance -> alliance.proposalsList }

    fun findAllPropsFromCategory(proposalEnum: ProposalEnum): List<Proposal>? =
        findAllProposals()?.filter { proposal ->
            proposal.proposalEnum == proposalEnum && proposal.initiator.id != Game.myId
        }

    fun findMyProposals(): List<Proposal>? =
        findAllProposals()?.filter { proposal -> proposal.initiator.id == Game.myId }

    fun findMyArmyRequests(): MutableList<ArmyRequest> = findPlayerByUUID(Game.myId).armyRequestReceived

    fun findPlayersOutsideAlliance(allianceName: String): MutableList<Player> {
        val answer = mutableListOf<Player>()
        val alliance: Alliance? = findAllianceByName(allianceName)
        Game.players.value?.forEach { player: Player ->
            alliance?.let {
                if (!playerBelongToAlliance(it, player)) {
                    answer.add(player)
                }
            }
        }
        return answer
    }

    fun findAllRegions(): List<Region> = Game.regions.filter { region -> region.occupiedBy != null }

    fun findAttackableRegions(): List<Region> =
        Game.regions.filter { region -> region.occupiedBy != null && region.occupiedBy!!.id != Game.myId }

    fun findRegionHolderId(regionName: String): Player? = findRegionByName(regionName)?.occupiedBy

    fun iCanAttackThisRegion(regionName: String): Boolean {
        val region: Region = findRegionByName(regionName)!!
        val meAsAPlayer: Player = findPlayerByUUID(Game.myId)
        if (region.occupiedBy == null || region.occupiedBy!!.id == Game.myId
            || !meAsAPlayer.army.sizePerRegion.containsKey(region.id))
            return false
        return true
    }

    fun soldiersForRegion(regionName: String, playerId: UUID): Int? {
        val region: Region = findRegionByName(regionName)!!
        val player = findPlayerByUUID(playerId)
        return player.army.sizePerRegion[region.id]
    }

    fun findRegionByName(regionName: String): Region? =
        Game.regions.find { region -> region.name == regionName }

    fun findRegionById(regionId: Int): Region? =
        Game.regions.find { region -> region.id == regionId }

    fun findRegionPopulation(regionName: String): List<RegionPlayerInfo>? {
        val region: Region = findRegionByName(regionName)!!
        return Game.players.value?.
        filter { player: Player -> player.army.sizePerRegion.containsKey(region.id) }
            ?.map { player -> RegionPlayerInfo(player.username,
                player.army.sizePerRegion[region.id]!!, regionName) }
    }

    fun iAmTheHost(): Boolean =
        Game.roomInfo?.username == findPlayerByUUID(Game.myId).username

    fun findRegionOwner(regionId: Int): Player? {
        Game.regions.forEach { region: Region ->
            if (region.id == regionId)
                return region.occupiedBy?.id?.let { findPlayerByUUID(it) }
        }
        return null
    }

    fun findPlayerRegions(playerId: UUID): MutableList<Int> =
        Game.regions.filter { region -> region.occupiedBy?.id == playerId }
            .map { region -> region.id }.toMutableList()

    fun findRegionDefOrAtt(regionId: Int, proposalEnum: ProposalEnum): Set<Player> {
        val ans: MutableSet<Player> = mutableSetOf()
        if (proposalEnum == ProposalEnum.DEFEND) {
            findRegionOwner(regionId)?.let { ans.add(it) }
        }
        Game.strategyActions.forEach { strategyAction: StrategyAction ->
            if (strategyAction.proposalEnum == proposalEnum && strategyAction.targetRegion == regionId) {
                ans.add(findPlayerByUUID(strategyAction.initiator.id))
                for (helper in strategyAction.helpers) {
                    ans.add(findPlayerByUUID(helper.id))
                }
            }
        }
        return ans
    }

    fun findRegionAttackInitiator(regionId: Int): Player? {
        return Game.strategyActions.filter { strategyAction ->
            strategyAction.proposalEnum == ProposalEnum.ATTACK &&
                    strategyAction.targetRegion == regionId }.firstOrNull()?.initiator
    }

    fun String.camelCaseToSpaced(): String {
        var ans = ""
        forEach { c: Char -> ans += if (c in 'A'..'Z') " $c" else c }
        return ans
    }

    fun getColorByPlayerIdx(idx: Int) = when(idx) {
        0 -> Color.GREEN
        1 -> Color.BLUE
        2 -> Color.CYAN
        3 -> Color.YELLOW
        4 -> Color.MAGENTA
        5 -> Color.DKGRAY
        6 -> Color.LTGRAY
        7 -> Color.TRANSPARENT
        8 -> Color.RED
        else -> Color.RED
    }

    fun getColorStringByPlayerIdx(idx: Int): String = when(idx) {
        0 -> "green"
        1 -> "blue"
        2 -> "cyan"
        3 -> "yellow"
        4 -> "magenta"
        5 -> "dark gray"
        6 -> "light gray"
        7 -> "transparent"
        8 -> "red"
        else -> "red"
    }

    fun roundTimeToString(roundTime: Int): String {
        return (roundTime / 60).toString() + ":" + (roundTime % 60).toString()
    }

}