package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.ProposalEnum
import java.io.Serializable
import java.util.*

class ProposalResponse(allianceId: UUID, proposalId: UUID, response: Boolean,
                       playerId: UUID) : Serializable