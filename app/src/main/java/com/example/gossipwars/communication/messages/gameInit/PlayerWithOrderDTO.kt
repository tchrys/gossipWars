package com.example.gossipwars.communication.messages.gameInit

import java.io.Serializable
import java.util.*

class PlayerWithOrderDTO(val order: Int, val username: String, val id: UUID) : Serializable