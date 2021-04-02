package com.example.gossipwars.logic.entities

import com.example.gossipwars.R
import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.proposals.ArmyOption
import com.example.gossipwars.logic.proposals.ArmyRequest
import java.util.*

object Snapshots {
    var fightsPerRound: MutableList<MutableList<Fight>> = mutableListOf()
    var troopsMovedPerRound: MutableList<MutableList<TroopsAction>> = mutableListOf()
    var armyImprovementsPerRound: MutableList<MutableList<ArmyRequest>> = mutableListOf()

    fun generateNewsFeed(): List<NewsfeedInfo> {
        val ans: MutableList<NewsfeedInfo> = mutableListOf()
        for (i in fightsPerRound.size - 1 downTo 0) {
            for (fight in fightsPerRound[i]) {
                if (fight.attackers.isNotEmpty() && fight.defenders.isNotEmpty()) {
                    ans.add(extractInfoFromFight(fight))
                }
            }
            for (troopAction in troopsMovedPerRound[i]) {
                ans.add(extractInfoFromTroops(troopAction))
            }
            for (armyUpgrade in armyImprovementsPerRound[i]) {
                ans.add(extractInfoFromArmyUpgrade(armyUpgrade))
            }
        }
        return ans
    }

    fun extractInfoFromArmyUpgrade(armyRequest: ArmyRequest): NewsfeedInfo {
        val title = String.format("%s's army is stronger now", armyRequest.initiator.username)
        val content = String.format(
            "%s raised his army's %s with %s%s",
            armyRequest.initiator.username,
            armyRequest.armyOption.toString().toLowerCase(Locale.ROOT),
            armyRequest.increase,
            if (armyRequest.armyOption == ArmyOption.SIZE) "" else "xp"
        )
        return NewsfeedInfo(armyRequest.initiator.capitalRegion, null, title, content)
    }

    fun extractInfoFromTroops(troopsAction: TroopsAction): NewsfeedInfo {
        val fromRegion: Region? = GameHelper.findRegionById(troopsAction.fromRegion)
        val toRegion: Region? = GameHelper.findRegionById(troopsAction.toRegion)
        val title = String.format("%s is moving troops", troopsAction.initiator.username)
        val content = String.format(
            "%s moved %s soldiers from %s to %s",
            troopsAction.initiator.username,
            troopsAction.size.toString(),
            fromRegion?.name,
            toRegion?.name
        )
        return NewsfeedInfo(troopsAction.toRegion, troopsAction.fromRegion, title, content)
    }

    fun extractInfoFromFight(fight: Fight): NewsfeedInfo {
        val title = String.format("Battle in %s", fight.region.name)
        var content: String = ""
        fight.attackers.forEachIndexed { index, player ->
            val conjunction = getConjunction(index, fight.attackers.size)
            content += player.username + conjunction
        }
        content += " fought "
        fight.defenders.forEachIndexed { index, player ->
            val conjunction = getConjunction(index, fight.defenders.size)
            content += player.username + conjunction
        }
        content += " for the control of " + fight.region.name
        return NewsfeedInfo(fight.region.id, null, title, content)
    }

    fun getConjunction(idx: Int, size: Int): String {
        return when (idx) {
            size - 1 -> ""
            size - 2 -> " and "
            else -> ", "
        }
    }

    fun getDrawableForRegion(regionId: Int): Int =
        when (regionId) {
            1 -> R.drawable.region1_map
            2 -> R.drawable.region2_map
            3 -> R.drawable.region3_map
            4 -> R.drawable.region4_map
            5 -> R.drawable.region5_map
            6 -> R.drawable.region6_map
            7 -> R.drawable.region7_map
            8 -> R.drawable.region8_map
            9 -> R.drawable.region9_map
            10 -> R.drawable.region10_map
            11 -> R.drawable.region11_map
            12 -> R.drawable.region12_map
            13 -> R.drawable.region13_map
            else -> R.drawable.region1_map
        }

}