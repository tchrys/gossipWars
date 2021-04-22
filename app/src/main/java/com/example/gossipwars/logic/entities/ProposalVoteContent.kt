package com.example.gossipwars.logic.entities

import com.example.gossipwars.logic.proposals.Proposal

data class ProposalVoteContent(val proposal: Proposal, val targetRegion: Int?, val membersRegions: MutableList<Int>?, val title: String, val content: String) {
}