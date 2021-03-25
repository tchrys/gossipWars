package com.example.gossipwars.ui.actions

import com.example.gossipwars.logic.proposals.ArmyRequest

class VoteNegotiateResult(val yesList: MutableList<ArmyRequest> = mutableListOf(),
                            val noList: MutableList<ArmyRequest> = mutableListOf())