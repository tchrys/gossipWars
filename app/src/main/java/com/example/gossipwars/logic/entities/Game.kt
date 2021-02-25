package com.example.gossipwars.logic.entities

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
    var regions: MutableList<Region> = mutableListOf()
    var alliances: MutableList<Alliance> = mutableListOf()
    var regionsPerPlayers: MutableMap<Int, UUID> = mutableMapOf()
    val noOfRegions: Int = 10
    var noOfRounds: Int = 0
    var roomInfo: RoomInfo? = null
    lateinit var mainActivity: MainActivity
    lateinit var myId: UUID
    var endpointToId: MutableMap<String, UUID> = mutableMapOf()
    var idToEndpoint: MutableMap<UUID, String> = mutableMapOf()
    var gameStarted = false

    init {
        regions = Region.initAllRegions()
    }

    fun convertUUIDToPlayer(lookupId: UUID): Player {
        return players.value?.find { player -> player.id == lookupId }!!
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
                roomInfo?.crtPlayersNr == players.value?.size
            ) {
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

    fun getKickablePlayers(alliance: Alliance): List<Player> = alliance.playersInvolved

    fun getJoinablePlayers(alliance: Alliance): List<Player>? =
        players.value?.filter { player -> !alliance.playersInvolved.contains(player) }

    fun addAlliance(player: Player, name: String) {
        val alliance: Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
    }

    fun sendAllianceDTO(allianceDTO: AllianceDTO, targetId: UUID) {
        val data = SerializationUtils.serialize(allianceDTO)
        val streamPayload = Payload.zza(data, MessageCode.ALLIANCE_INFO.toLong())
        idToEndpoint[targetId]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
        }
    }

    fun receiveNewAllianceInfo(allianceDTO: AllianceDTO) {
        val alliance: Alliance = allianceDTO.convertToAlliance()
        alliances.add(alliance)
        alliance.addPlayer(convertUUIDToPlayer(myId))
    }

    fun sendJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        val data = SerializationUtils.serialize(joinKickProposalDTO)
        val streamPayload = Payload.zza(data, MessageCode.JOIN_KICK_PROPOSAL.toLong())
        var alliance: Alliance? =
            alliances.find { alliance -> alliance.id == joinKickProposalDTO.allianceId }
        for (playerInvolved in alliance?.playersInvolved!!) {
            if (ProposalEnum.KICK == joinKickProposalDTO.proposalEnum && playerInvolved.id == joinKickProposalDTO.target)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity)
                    .sendPayload(it, streamPayload)
            };
        }
    }

    fun sendMembersAction(membersAction: MembersAction) {
        val data = SerializationUtils.serialize(membersAction)
        val streamPayload = Payload.zza(data, MessageCode.MEMBERS_ACTION.toLong())
        var alliance: Alliance? =
            alliances.find { alliance -> alliance.id == membersAction.allianceId }
        for (playerInvolved in alliance?.playersInvolved!!) {
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        if (ProposalEnum.JOIN == membersAction.proposalEnum) {
            sendAllianceDTO(alliance.convertToAllianceDTO(), membersAction.targetId)
        }
    }

    fun acknowledgeMembersAction(membersAction: MembersAction) {
        var alliance: Alliance? =
            alliances.find { alliance -> alliance.id == membersAction.allianceId }
        var targetPlayer: Player? =
            players.value?.find { player -> player.id == membersAction.targetId }
        if (targetPlayer != null) {
            if (ProposalEnum.JOIN == membersAction.proposalEnum) {
                alliance?.addPlayer(targetPlayer)
            } else if (ProposalEnum.KICK == membersAction.proposalEnum) {
                alliance?.kickPlayer(targetPlayer)
            }
        }
    }

}