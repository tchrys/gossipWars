package com.example.gossipwars.logic.entities

import com.example.gossipwars.R
import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.entities.GameHelper.camelCaseToSpaced
import com.example.gossipwars.logic.proposals.*
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

    private fun getMembersRegionsList(alliance: Alliance): MutableList<Int> {
        val ans = mutableListOf<Int>()
        for (player in alliance.playersInvolved) {
            if (player.capitalRegion != -1) {
                ans.add(player.capitalRegion)
            }
        }
        return ans
    }

    private fun getMembersRegionsString(alliance: Alliance): String {
        var content = ""
        var idx = 0
        for (player in alliance.playersInvolved) {
            content += "\u25BA ${player.username}"
            content += if (player.capitalRegion == -1) {
                "(no capital)\n"
            } else {
                " with capital in ${GameHelper.findRegionById(player.capitalRegion)?.name?.camelCaseToSpaced()} (${GameHelper.getColorStringByPlayerIdx(idx)})\n"
            }
            idx += 1
        }
        return content
    }

    fun generateContentFromJoinProposal(proposal: JoinProposal): ProposalVoteContent {
        val content = getMembersRegionsString(proposal.alliance)
        val membersRegions = getMembersRegionsList(proposal.alliance)
        return ProposalVoteContent(
            proposal,
            null,
            membersRegions,
            "${proposal.initiator.username} want ${proposal.target.username}" +
                    "to be part of ${proposal.alliance.name}",
            "The members of this alliance are:\n${content}"
        )
    }

    fun generateContentFromKickProposal(proposal: KickProposal): ProposalVoteContent {
        val content = getMembersRegionsString(proposal.alliance)
        val membersRegions = getMembersRegionsList(proposal.alliance)
        return ProposalVoteContent(
            proposal,
            null,
            membersRegions,
            "${proposal.initiator.username} want ${proposal.target.username}" +
                    "to be kicked from ${proposal.alliance.name}",
            "The members of this alliance are:\n${content}"
        )
    }

    fun generateContentFromAttackProposal(proposal: StrategyProposal): ProposalVoteContent {
        val content = getMembersRegionsString(proposal.alliance)
        val membersRegions = getMembersRegionsList(proposal.alliance)
        return ProposalVoteContent(
            proposal,
            proposal.targetRegion,
            membersRegions,
            "${proposal.initiator.username} requests ${proposal.alliance.name}'s members to " +
                    "attack ${GameHelper.findRegionById(proposal.targetRegion)?.name?.camelCaseToSpaced()}",
            "The members of this alliance are:\n${content}"
        )
    }

    fun generateContentFromDefendProposal(proposal: StrategyProposal): ProposalVoteContent {
        val content = getMembersRegionsString(proposal.alliance)
        val membersRegions = getMembersRegionsList(proposal.alliance)
        return ProposalVoteContent(
            proposal,
            proposal.targetRegion,
            membersRegions,
            "${proposal.initiator.username} requests ${proposal.alliance.name}'s members to " +
                    "defend ${GameHelper.findRegionById(proposal.targetRegion)?.name?.camelCaseToSpaced()}",
            "The members of this alliance are:\n${content}"
        )
    }

    fun generateContentFromNegotiateProposal(armyRequest: ArmyRequest): NegotiateVoteContent {
        val increaseUnit: String = if (armyRequest.armyOption != ArmyOption.SIZE) "xp" else "soldiers"
        return NegotiateVoteContent(
            armyRequest,
            if (armyRequest.initiator.capitalRegion == -1) null else armyRequest.initiator.capitalRegion ,
            GameHelper.findPlayerRegions(armyRequest.initiator.id),
            "Proposal from ${armyRequest.initiator.username}",
            "${armyRequest.initiator.username} wants to increase his army" +
                    armyRequest.armyOption.toString().toLowerCase(Locale.ROOT) +
                    "with ${armyRequest.increase} $increaseUnit"
        )
    }

    private fun ArmyRequest.extractInfoFromArmyUpgrade(): NewsFeedInfo {
        val title = "${initiator.username}'s army is stronger now"
        var content = String.format(
            "%s raised his army's %s with %s%s.",
            initiator.username,
            armyOption.toString().toLowerCase(Locale.ROOT),
            increase,
            if (armyOption == ArmyOption.SIZE) "" else "xp"
        )
        if (approver.username != initiator.username)
            content += "${approver.username} approved this request."
        return NewsFeedInfo(initiator.capitalRegion, null, title, content)
    }

    private fun TroopsAction.extractInfoFromTroops(): NewsFeedInfo {
        val fromName: String? = GameHelper.findRegionById(fromRegion)?.name?.camelCaseToSpaced()
        val toName: String? = GameHelper.findRegionById(toRegion)?.name?.camelCaseToSpaced()
        val title = "${initiator.username} is moving troops"
        val content = "${initiator.username} moved $size soldiers from $fromName to $toName"
        return NewsFeedInfo(this.toRegion, this.fromRegion, title, content)
    }

    private fun Fight.extractInfoFromFight(): NewsFeedInfo {
        val title = "Battle in ${region.name.camelCaseToSpaced()}"
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
        content += " for the control of ${region.name.camelCaseToSpaced()}"
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
            -1 -> R.drawable.map_smaller
            else -> R.drawable.map
        }

}