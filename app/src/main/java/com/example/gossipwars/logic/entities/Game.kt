package com.example.gossipwars.logic.entities

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import org.apache.commons.lang3.SerializationUtils
import java.util.*

object Game {
    var players = MutableLiveData<MutableList<Player>>().apply {
        value = mutableListOf()
    }
    var regions : MutableList<Region> = mutableListOf()
    var alliances : MutableList<Alliance> = mutableListOf()
    var regionsPerPlayers : MutableMap<Int, UUID> = mutableMapOf()
    val noOfRegions : Int = 10;
    var noOfRounds : Int = 0;
    var roomInfo : RoomInfo? = null
    lateinit var mainActivity: MainActivity
    lateinit var myId: UUID
    var endpointToId : MutableMap<String, UUID> = mutableMapOf()
    var idToEndpoint: MutableMap<UUID, String> = mutableMapOf()
    var gameStarted = false

    init {
        regions = Region.initAllRegions()
    }

    fun sendMyInfo() {
        val meAsAPlayer = players.value?.get(0)
        val myPlayerDTO = meAsAPlayer?.username?.let { PlayerDTO(it, meAsAPlayer?.id) }
        val data = SerializationUtils.serialize(myPlayerDTO)
        val streamPayload = Payload.zza(data, MessageCode.PLAYER_INFO.toLong())
        for (playerEndpoint in mainActivity.peers) {
            Nearby.getConnectionsClient(mainActivity).sendPayload(playerEndpoint, streamPayload)
        }
    }

    fun acknowledgePlayer(playerDTO: PlayerDTO, endpointId: String) {
        if (players?.value?.find { player -> player.id == playerDTO.id } == null) {
            players.value?.add(Player(playerDTO.username, playerDTO.id))
            endpointToId[endpointId] = playerDTO.id
            idToEndpoint[playerDTO.id] = endpointId
            if (roomInfo?.username == players?.value?.get(0)?.username &&
                roomInfo?.crtPlayersNr == players.value?.size) {
                sendOrderPayload()
            }
        }
    }

    private fun sendOrderPayload() {
        var ans = mutableListOf<PlayerWithOrder>()
        players.value?.forEachIndexed { index, player ->
            ans.add(PlayerWithOrder(index, player.username, player.id))
        }
        var playersOrderDTO = PlayersOrderDTO(ans)
        val data = SerializationUtils.serialize(playersOrderDTO)
        val streamPayload = Payload.zza(data, MessageCode.PLAYER_ORDER.toLong())
        for (playerEndpoint in mainActivity.peers) {
            Nearby.getConnectionsClient(mainActivity).sendPayload(playerEndpoint, streamPayload)
        }
        reorderPlayers(playersOrderDTO)
    }

    fun reorderPlayers(playersOrderDTO: PlayersOrderDTO) {
        players.value?.clear()
        for (player in playersOrderDTO.players) {
            players.value?.add(Player(player.username, player.id))
        }
        initGame()
    }

    private fun initGame() {
        if (gameStarted)
            return
        var playersIds = players.value?.map { player -> player.id }
        players.value?.forEachIndexed { index, player ->
            regionsPerPlayers[index] = player.id
            player.army = Army.initDefaultArmy(regions[index])
            player.winRegion(regions[index])
            if (player.id == myId) {
                if (playersIds != null) {
                    for (playerId in playersIds) {
                        if (playerId != myId) {
                            player.trustInOthers[playerId] = 5
                        }
                    }
                }
            }
        }
        gameStarted = true
    }

    fun getKickablePlayers(alliance: Alliance) : List<Player> = alliance.playersInvolved

    fun getJoinablePlayers(alliance: Alliance) : List<Player>? =
        players.value?.filter{ player -> !alliance.playersInvolved.contains(player) }

    fun addAlliance(player: Player, name : String) {
        val alliance : Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
        player.joinAlliance(alliance)
    }

}