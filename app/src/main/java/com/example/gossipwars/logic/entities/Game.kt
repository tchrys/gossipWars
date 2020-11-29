package com.example.gossipwars.logic.entities

import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import org.apache.commons.lang3.SerializationUtils
import java.util.*

object Game {
    var players : MutableList<Player> = mutableListOf()
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

    init {
        regions = Region.initAllRegions()
    }

    fun sendMyInfo() {
        val meAsAPlayer = players.get(0)
        val myPlayerDTO = PlayerDTO(meAsAPlayer.username, meAsAPlayer.id)
        val data = SerializationUtils.serialize(myPlayerDTO)
        val streamPayload = Payload.zza(data, MessageCode.PLAYER_INFO.toLong())
        for (playerEndpoint in roomInfo?.playersList!!) {
            Nearby.getConnectionsClient(mainActivity).sendPayload(playerEndpoint, streamPayload)
        }
    }

    fun acknowledgePlayer(playerDTO: PlayerDTO, endpointId: String) {
        players.add(Player(playerDTO.username, playerDTO.id))
        endpointToId[endpointId] = playerDTO.id
        idToEndpoint[playerDTO.id] = endpointId
        if (roomInfo?.username == players[0].username && roomInfo?.crtPlayersNr == players.size) {
            sendOrderPayload()
        }
    }

    private fun sendOrderPayload() {
        var ans = mutableListOf<PlayerWithOrder>()
        players.forEachIndexed { index, player ->
            ans.add(PlayerWithOrder(index, player.username, player.id))
        }
        val data = SerializationUtils.serialize(PlayersOrderDTO(ans))
        val streamPayload = Payload.zza(data, MessageCode.PLAYER_ORDER.toLong())
        for (playerEndpoint in roomInfo?.playersList!!) {
            Nearby.getConnectionsClient(mainActivity).sendPayload(playerEndpoint, streamPayload)
        }
        initGame()
    }

    fun reorderPlayers(playersOrderDTO: PlayersOrderDTO) {
        players.clear()
        for (player in playersOrderDTO.players) {
            players.add(Player(player.username, player.id))
        }
        initGame()
    }

    private fun initGame() {
        var playersIds = players.map { player -> player.id }
        players.forEachIndexed { index, player ->
            regionsPerPlayers[index] = player.id
            player.army = Army.initDefaultArmy(regions[index])
            player.winRegion(regions[index])
            if (player.id == myId) {
                for (playerId in playersIds) {
                    if (playerId != myId) {
                        player.trustInOthers[playerId] = 5
                    }
                }
            }
        }
    }

    fun getKickablePlayers(alliance: Alliance) : List<Player> = alliance.playersInvolved

    fun getJoinablePlayers(alliance: Alliance) : List<Player> =
        players.filter{ player -> !alliance.playersInvolved.contains(player) }

    fun addAlliance(player: Player, name : String) {
        val alliance : Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
        player.joinAlliance(alliance)
    }

}