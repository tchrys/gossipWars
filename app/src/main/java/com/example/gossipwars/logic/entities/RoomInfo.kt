package com.example.gossipwars.logic.entities

class RoomInfo(val username: String, var roomName : String, var roundLength: Int,
               var maxPlayers : Int, var crtPlayersNr : Int = 1,
                var playersList: MutableList<String> = mutableListOf()) {
}