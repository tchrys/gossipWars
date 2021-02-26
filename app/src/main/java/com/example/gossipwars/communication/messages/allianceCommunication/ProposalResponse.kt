package com.example.gossipwars.communication.messages.allianceCommunication

import java.io.Serializable
import java.util.*

class ProposalResponse(val allianceId: UUID, val proposalId: UUID, val response: Boolean,
                       val playerId: UUID) : Serializable