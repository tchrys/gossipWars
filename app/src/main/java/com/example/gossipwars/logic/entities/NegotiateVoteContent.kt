package com.example.gossipwars.logic.entities

import com.example.gossipwars.logic.proposals.ArmyRequest

data class NegotiateVoteContent(val armyRequest: ArmyRequest, val targetRegion: Int?, val membersRegions: MutableList<Int>?, val title: String, val content: String) {
}