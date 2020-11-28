package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.entities.ProposalEnum
import java.util.*

class StrategyProposalDTO(val allianceId: UUID, val target: UUID, val initiator: UUID,
                          val targetRegion: Int, val proposalEnum: ProposalEnum, val proposalId: UUID)