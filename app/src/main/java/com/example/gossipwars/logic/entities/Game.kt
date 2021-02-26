package com.example.gossipwars.logic.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.*
import com.example.gossipwars.communication.messages.actions.*
import com.example.gossipwars.communication.messages.allianceCommunication.ChatMessageDTO
import com.example.gossipwars.communication.messages.allianceCommunication.JoinKickProposalDTO
import com.example.gossipwars.communication.messages.allianceCommunication.ProposalResponse
import com.example.gossipwars.communication.messages.allianceCommunication.StrategyProposalDTO
import com.example.gossipwars.communication.messages.gameInit.PlayerDTO
import com.example.gossipwars.communication.messages.gameInit.PlayerWithOrderDTO
import com.example.gossipwars.communication.messages.gameInit.PlayersOrderDTO
import com.example.gossipwars.communication.messages.gameInit.RoomInfoDTO
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.proposals.Proposal
import com.example.gossipwars.logic.proposals.ProposalEnum
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
    var playersNoWithActionSent: MutableSet<UUID> = mutableSetOf()
    var strategyActions: MutableList<StrategyAction> = mutableListOf()
    var troopsActions: MutableList<TroopsAction> = mutableListOf()
    var roomInfo: RoomInfoDTO? = null
    lateinit var mainActivity: MainActivity
    lateinit var myId: UUID
    var endpointToId: MutableMap<String, UUID> = mutableMapOf()
    var idToEndpoint: MutableMap<UUID, String> = mutableMapOf()
    var gameStarted = false

    init {
        regions = Region.initAllRegions()
    }

    fun findPlayerByUUID(lookupId: UUID): Player {
        return players.value?.find { player -> player.id == lookupId }!!
    }

    fun findAllianceByUUID(lookupId: UUID): Alliance {
        return alliances.find { alliance -> alliance.id == lookupId }!!
    }

    fun iAmTheHost(): Boolean {
        var meAsAPlayer = findPlayerByUUID(myId)
        return roomInfo?.username == meAsAPlayer.username
    }

    fun addAlliance(player: Player, name: String) {
        val alliance: Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
    }

    fun findRegionOwner(regionId: Int): Player? {
        regions.forEach { region: Region ->
            if (region.id == regionId)
                return region.occupiedBy
        }
        return null
    }

    fun findRegionDefOrAtt(regionId: Int, proposalEnum: ProposalEnum): Set<Player> {
        var ans: MutableSet<Player> = mutableSetOf()
        if (proposalEnum == ProposalEnum.DEFEND) {
            findRegionOwner(regionId)?.let { ans.add(it) }
        }
        strategyActions.forEach { strategyAction: StrategyAction ->
            if (strategyAction.proposalEnum == proposalEnum) {
                ans.add(strategyAction.initiator)
                for (helper in strategyAction.helpers) {
                    ans.add(helper)
                }
            }
        }
        return ans
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun roundEndCompute() {
        // strategy actions
        for (region in regions) {
            val attackers: Set<Player> = findRegionDefOrAtt(region.id, ProposalEnum.ATTACK)
            val defenders: Set<Player> = findRegionDefOrAtt(region.id, ProposalEnum.DEFEND)
            TODO("simulate fight")
        }
        // troop actions
        for (troopAction in troopsActions) {
            troopAction.initiator.moveTroops(troopAction.fromRegion, troopAction.toRegion, troopAction.size)
        }
    }

    fun sendMyInfo() {
        val meAsAPlayer = players.value?.get(0)
        val myPlayerDTO = meAsAPlayer?.username?.let {
            PlayerDTO(
                it,
                meAsAPlayer?.id
            )
        }
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
        var ans = mutableListOf<PlayerWithOrderDTO>()
        players.value?.forEachIndexed { index, player ->
            ans.add(
                PlayerWithOrderDTO(
                    index,
                    player.username,
                    player.id
                )
            )
        }
        var playersOrderDTO =
            PlayersOrderDTO(
                ans
            )
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

    fun sendAllianceDTO(allianceInvitationDTO: AllianceInvitationDTO, targetId: UUID) {
        val data = SerializationUtils.serialize(allianceInvitationDTO)
        val streamPayload = Payload.zza(data, MessageCode.ALLIANCE_INFO.toLong())
        idToEndpoint[targetId]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
        }
    }

    fun receiveNewAllianceInfo(allianceInvitationDTO: AllianceInvitationDTO) {
        val alliance: Alliance = allianceInvitationDTO.convertToEntity()
        alliances.add(alliance)
        alliance.addPlayer(findPlayerByUUID(myId))
    }

    fun sendJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        val data = SerializationUtils.serialize(joinKickProposalDTO)
        val streamPayload = Payload.zza(data, MessageCode.JOIN_KICK_PROPOSAL.toLong())
        var alliance: Alliance? =
            alliances.find { alliance -> alliance.id == joinKickProposalDTO.allianceId }
        for (playerInvolved in alliance?.playersInvolved!!) {
            if (playerInvolved.id == myId)
                continue
            if (ProposalEnum.KICK == joinKickProposalDTO.proposalEnum && playerInvolved.id == joinKickProposalDTO.target)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity)
                    .sendPayload(it, streamPayload)
            }
        }
    }

    fun receiveJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        var alliance: Alliance? = alliances.find { alliance -> alliance.id == joinKickProposalDTO.allianceId }
        var targetPlayer: Player? = players.value?.find { player -> player.id == joinKickProposalDTO.target }
        var initiator: Player? = players.value?.find { player -> player.id == joinKickProposalDTO.initiator }
        if (targetPlayer != null && initiator != null) {
            alliance?.addProposal(targetPlayer, initiator, joinKickProposalDTO.proposalId, joinKickProposalDTO.proposalEnum, 0)
        }
    }

    fun sendMembersAction(membersAction: MembersActionDTO) {
        val data = SerializationUtils.serialize(membersAction)
        val streamPayload = Payload.zza(data, MessageCode.MEMBERS_ACTION.toLong())
        var alliance: Alliance? =
            alliances.find { alliance -> alliance.id == membersAction.allianceId }
        for (playerInvolved in alliance?.playersInvolved!!) {
            if (playerInvolved.id == myId)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        if (ProposalEnum.JOIN == membersAction.proposalEnum) {
            sendAllianceDTO(alliance.convertToDTO(), membersAction.targetId)
        }
        acknowledgeMembersAction(membersAction)
    }

    fun acknowledgeMembersAction(membersAction: MembersActionDTO) {
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

    fun sendProposalResponse(proposalResponse: ProposalResponse) {
        val data = SerializationUtils.serialize(proposalResponse)
        val streamPayload = Payload.zza(data, MessageCode.PROPOSAL_RESPONSE.toLong())
        var alliance: Alliance? = alliances.find { alliance -> alliance.id == proposalResponse.allianceId }
        var proposal: Proposal? = alliance?.proposalsList?.find { proposal ->
                                        proposal.proposalId == proposalResponse.proposalId }
        var meAsAPlayer: Player = findPlayerByUUID(myId)
        proposal?.votes?.set(meAsAPlayer, proposalResponse.response)
        idToEndpoint[proposal?.initiator?.id]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(
                it, streamPayload)
        }
    }

    fun receiveProposalResponse(proposalResponse: ProposalResponse) {
        var alliance: Alliance? = alliances.find { alliance -> alliance.id == proposalResponse.allianceId }
        var proposal: Proposal? = alliance?.proposalsList?.find { proposal ->
            proposal.proposalId == proposalResponse.proposalId }
        var sender: Player? = players.value?.find { player -> player.id == proposalResponse.playerId }
        if (sender != null) {
            proposal?.registerVote(sender, proposalResponse.response)
        }
    }

    fun sendMessage(message: ChatMessageDTO) {
        val data = SerializationUtils.serialize(message)
        val streamPayload = Payload.zza(data, MessageCode.MESSAGE_DTO.toLong())
        var alliance: Alliance? =
            alliances.find { alliance -> alliance.id == message.allianceId }
        for (playerInvolved in alliance?.playersInvolved!!) {
            if (playerInvolved.id == myId)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        alliance.addMessage(message.convertToEntity())
    }

    fun receiveMessage(message: ChatMessageDTO) {
        var alliance: Alliance? = alliances.find { alliance -> alliance.id == message.allianceId }
        alliance?.addMessage(message.convertToEntity())
    }

    fun acknowledgeActionEnd(actionsEndDTO: ActionEndDTO) {
        playersNoWithActionSent.add(actionsEndDTO.playerId)
        if (playersNoWithActionSent.size == players.value?.size) {
            // all players sent their actions, can start round end processing
            Game.roundEndCompute()
            TODO("logic for starting a new round, sth like two phase commit")
        }
    }

    fun sendActionEnd(actionsEndDTO: ActionEndDTO) {
        val data = SerializationUtils.serialize(actionsEndDTO)
        val streamPayload = Payload.zza(data, MessageCode.ACTION_END.toLong())
        for (player in players.value!!) {
            if (player.id == myId) continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        playersNoWithActionSent.add(myId)
    }

    fun receiveStrategyAction(strategyActionDTO: StrategyActionDTO) {
        strategyActions.add(strategyActionDTO.convertToStrategyAction())
    }

    fun sendStrategyAction(strategyActionDTO: StrategyActionDTO) {
        val data = SerializationUtils.serialize(strategyActionDTO)
        val streamPayload = Payload.zza(data, MessageCode.STRATEGY_ACTION.toLong())
        for (helper in strategyActionDTO.helpers) {
            if (helper == myId)
                continue
            idToEndpoint[helper]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        strategyActions.add(strategyActionDTO.convertToStrategyAction())
    }

    fun receiveStrategyProposal(strategyProposalDTO: StrategyProposalDTO) {
        val alliance: Alliance = findAllianceByUUID(strategyProposalDTO.allianceId)
        alliance.addProposal(
            findPlayerByUUID(strategyProposalDTO.target),
                             findPlayerByUUID(strategyProposalDTO.initiator),
                             strategyProposalDTO.proposalId, strategyProposalDTO.proposalEnum,
                             strategyProposalDTO.targetRegion)
    }

    fun sendStrategyProposal(strategyProposalDTO: StrategyProposalDTO) {
        val data = SerializationUtils.serialize(strategyProposalDTO)
        val streamPayload = Payload.zza(data, MessageCode.STRATEGY_PROPOSAL.toLong())
        val alliance: Alliance = findAllianceByUUID(strategyProposalDTO.allianceId)
        for (playerInvolved in alliance.playersInvolved) {
            if (playerInvolved.id == myId)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
    }

    fun receiveTroopsAction(troopsActionDTO: TroopsActionDTO) {
        troopsActions.add(troopsActionDTO.convertToEntity())
    }

    fun sendTroopsAction(troopsActionDTO: TroopsActionDTO) {
        val data = SerializationUtils.serialize(troopsActionDTO)
        val streamPayload = Payload.zza(data, MessageCode.TROOPS_ACTION.toLong())
        for (player in players.value!!) {
            if (player.id == myId)
                continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        troopsActions.add(troopsActionDTO.convertToEntity())
    }

}