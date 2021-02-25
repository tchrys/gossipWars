package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.entities.ProposalEnum
import java.io.Serializable
import java.util.*

class JoinKickProposalDTO(val allianceId: UUID, val target: UUID, val initiator: UUID,
                          val proposalId: UUID, val proposalEnum: ProposalEnum) : Serializable