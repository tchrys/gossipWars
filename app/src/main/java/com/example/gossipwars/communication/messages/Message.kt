package com.example.gossipwars.communication.messages

import java.io.Serializable
import java.util.*

class Message(val content: String, val sender : UUID) : Serializable