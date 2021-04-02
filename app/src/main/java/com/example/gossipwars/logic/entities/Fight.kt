package com.example.gossipwars.logic.entities

data class Fight(val attackers: Set<Player>, val defenders: Set<Player>, val region: Region) {
}