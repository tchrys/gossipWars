package com.example.gossipwars.communication.messages

import java.io.Serializable
import java.util.*

class PlayerWithOrder(val order: Int, val username: String, val id: UUID) : Serializable