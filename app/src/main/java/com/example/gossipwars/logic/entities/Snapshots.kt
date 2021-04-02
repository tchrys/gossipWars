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

    fun generateNewsFeed(): List<NewsFeedInfo> {
        val ans: MutableList<NewsFeedInfo> = mutableListOf()
        for (i in fightsPerRound.size - 1 downTo 0) {
            for (fight in fightsPerRound[i]) {
                if (fight.attackers.isNotEmpty() && fight.defenders.isNotEmpty()) {
                    ans.add(fight.extractInfoFromFight())
                }
            }
            for (troopAction in troopsMovedPerRound[i]) {
                ans.add(troopAction.extractInfoFromTroops())
            }
            for (armyUpgrade in armyImprovementsPerRound[i]) {
                ans.add(armyUpgrade.extractInfoFromArmyUpgrade())
            }
        }
        return ans
    }

    private fun ArmyRequest.extractInfoFromArmyUpgrade(): NewsFeedInfo {
        val title = "${initiator.username}'s army is stronger now"
        val content = String.format(
            "%s raised his army's %s with %s%s",
            initiator.username,
            armyOption.toString().toLowerCase(Locale.ROOT),
            increase,
            if (armyOption == ArmyOption.SIZE) "" else "xp"
        )
        return NewsFeedInfo(initiator.capitalRegion, null, title, content)
    }

    private fun TroopsAction.extractInfoFromTroops(): NewsFeedInfo {
        val fromName: String? = GameHelper.findRegionById(fromRegion)?.name
        val toName: String? = GameHelper.findRegionById(toRegion)?.name
        val title = "${initiator.username} is moving troops"
        val content = "${initiator.username} moved $size soldiers from $fromName to $toName"
        return NewsFeedInfo(this.toRegion, this.fromRegion, title, content)
    }

    private fun Fight.extractInfoFromFight(): NewsFeedInfo {
        val title = "Battle in ${region.name}"
        var content = ""
        attackers.forEachIndexed { index, player ->
            val conjunction = getConjunction(index, attackers.size)
            content += player.username + conjunction
        }
        content += " fought "
        defenders.forEachIndexed { index, player ->
            val conjunction = getConjunction(index, defenders.size)
            content += player.username + conjunction
        }
        content += " for the control of ${region.name}"
        return NewsFeedInfo(region.id, null, title, content)
    }

    private fun getConjunction(idx: Int, size: Int): String {
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