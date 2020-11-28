package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.entities.ProposalEnum
import java.util.*

class JoinKickProposalDTO(allianceId: UUID, target: UUID, initiator: UUID, proposalId: UUID,
                          proposalEnum: ProposalEnum)