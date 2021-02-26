package com.example.gossipwars.communication.messages.gameInit

import java.io.Serializable

class RoomInfoDTO(val username: String, var roomName : String, var roundLength: Int,
                  var maxPlayers : Int, var crtPlayersNr : Int = 1, var started: Boolean,
                  var playersList: MutableSet<String> = mutableSetOf()) : Serializable