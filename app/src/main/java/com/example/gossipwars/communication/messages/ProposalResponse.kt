package com.example.gossipwars.communication.messages

import com.example.gossipwars.logic.entities.ProposalEnum
import java.io.Serializable
import java.util.*

class ProposalResponse(val allianceId: UUID, val proposalId: UUID, val response: Boolean,
                       val playerId: UUID) : Serializable