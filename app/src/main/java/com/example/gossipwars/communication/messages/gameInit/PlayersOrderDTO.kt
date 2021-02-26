package com.example.gossipwars.communication.messages.gameInit

import java.io.Serializable

class PlayersOrderDTO(val players: List<PlayerWithOrderDTO>) : Serializable