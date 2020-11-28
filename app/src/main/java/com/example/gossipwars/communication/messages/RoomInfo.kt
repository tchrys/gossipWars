package com.example.gossipwars.communication.messages

import java.io.Serializable

class RoomInfo(val username: String, var roomName : String, var roundLength: Int,
               var maxPlayers : Int, var crtPlayersNr : Int = 1, var started: Boolean,
                var playersList: MutableSet<String> = mutableSetOf()) : Serializable {
}