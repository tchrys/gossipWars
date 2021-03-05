package com.example.gossipwars.logic.entities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.*
import com.example.gossipwars.communication.messages.actions.*
import com.example.gossipwars.communication.messages.allianceCommunication.*
import com.example.gossipwars.communication.messages.gameInit.PlayerDTO
import com.example.gossipwars.communication.messages.gameInit.PlayerWithOrderDTO
import com.example.gossipwars.communication.messages.gameInit.PlayersOrderDTO
import com.example.gossipwars.communication.messages.gameInit.RoomInfoDTO
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.proposals.ArmyRequest
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
    var playersWithAllActionsSent: MutableSet<UUID> = mutableSetOf()
    var strategyActions: MutableList<StrategyAction> = mutableListOf()
    var troopsActions: MutableList<TroopsAction> = mutableListOf()
    var armyActions: MutableList<ArmyRequest> = mutableListOf()
    var roomInfo: RoomInfoDTO? = null
    lateinit var mainActivity: MainActivity
    lateinit var myId: UUID
    var endpointToId: MutableMap<String, UUID> = mutableMapOf()
    var idToEndpoint: MutableMap<UUID, String> = mutableMapOf()
    var gameStarted = false
    var myBonusTaken: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        regions = Region.initAllRegions()
        myBonusTaken.value = false
    }

    fun findPlayerByUUID(lookupId: UUID): Player {
        return players.value?.find { player -> player.id == lookupId }!!
    }

    fun findPlayerByUsername(username: String): Player? {
        return players.value?.find { player -> player.username == username }
    }

    fun findAllianceByUUID(lookupId: UUID): Alliance {
        return alliances.find { alliance -> alliance.id == lookupId }!!
    }

    fun findAllianceByName(allianceName: String): Alliance? {
        return alliances.find { alliance -> alliance.name == allianceName }
    }

    fun findPlayersInsideAlliance(allianceName: String): MutableList<Player>? {
        val alliance: Alliance? = findAllianceByName(allianceName)
        return alliance?.playersInvolved?.filter { it.id !== myId }?.toMutableList()
    }

    fun playerBelongToAlliance(alliance: Alliance, player: Player): Boolean {
        return alliance.playersInvolved.any { playerInv -> playerInv.id == player.id }
    }

    fun findAlliancesForPlayer(playerId: UUID): MutableList<Alliance>? {
        return alliances.filter { alliance ->
            alliance.playersInvolved.any { player -> player.id == playerId }
        }.toMutableList()
    }

    fun findPlayersOutsideAlliance(allianceName: String): MutableList<Player> {
        var answer = mutableListOf<Player>()
        val alliance: Alliance? = findAllianceByName(allianceName)
        Game.players.value?.forEach { player: Player ->
            alliance?.let {
                if (!playerBelongToAlliance(it, player)) {
                    answer.add(player)
                }
            }
        }
        return answer
    }

    fun iAmTheHost(): Boolean {
        var meAsAPlayer = findPlayerByUUID(myId)
        return roomInfo?.username == meAsAPlayer.username
    }

    fun addAlliance(player: Player, name: String): Alliance {
        val alliance: Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
        return alliance
    }

    fun findRegionOwner(regionId: Int): Player? {
        regions.forEach { region: Region ->
            if (region.id == regionId)
                return region.occupiedBy?.id?.let { findPlayerByUUID(it) }
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
                ans.add(findPlayerByUUID(strategyAction.initiator.id))
                for (helper in strategyAction.helpers) {
                    ans.add(findPlayerByUUID(helper.id))
                }
            }
        }
        return ans
    }

    fun simulateFight(attackers: Set<Player>, defenders: Set<Player>, region: Region) {
        var attDamage = attackers.map { player -> player.getArmyAttDamage(region.id)}.sum()
        var attSize = attackers.map { player -> player.getArmySizeForRegion(region.id) }.sum()
        var defDamage = defenders.map { player -> player.getArmyDefDamage(region.id) }.sum()
        var defSize = defenders.map { player -> player.getArmySizeForRegion(region.id) }.sum()
        for (defender in defenders) {
            val defWeight = 1f * defender.getArmySizeForRegion(region.id) / defSize
            val soldiersLost = (attDamage * defWeight).toInt()
            defender.changeArmySizeForRegion(region.id, -soldiersLost)
        }
        for (attacker in attackers) {
            val attWeight = 1f * attacker.getArmySizeForRegion(region.id) / attSize
            val soldiersLost = (defDamage * attWeight).toInt()
            attacker.changeArmySizeForRegion(region.id, -soldiersLost)
        }
    }

    fun roundEndCompute() {
        // strategy actions
        for (region in regions) {
            val attackers: Set<Player> = findRegionDefOrAtt(region.id, ProposalEnum.ATTACK)
            val defenders: Set<Player> = findRegionDefOrAtt(region.id, ProposalEnum.DEFEND)
            simulateFight(attackers, defenders, region)
        }
        // troop actions
        for (troopAction in troopsActions) {
            troopAction.initiator.moveTroops(troopAction.fromRegion, troopAction.toRegion, troopAction.size)
        }
        // army actions
        for (armyAction in armyActions) {
            armyAction.initiator.improveArmy(armyAction)
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
        // register new member
        var alliance: Alliance = findAllianceByUUID(allianceInvitationDTO.id)
        var player: Player = findPlayerByUUID(targetId)
        alliance.addPlayer(player)
    }

    fun receiveNewAllianceInfo(allianceInvitationDTO: AllianceInvitationDTO) {
        val alliance: Alliance = allianceInvitationDTO.convertToEntity()
        alliances.add(alliance)
        alliance.addPlayer(findPlayerByUUID(myId))
    }

    fun sendJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        val data = SerializationUtils.serialize(joinKickProposalDTO)
        val streamPayload = Payload.zza(data, MessageCode.JOIN_KICK_PROPOSAL.toLong())
        var alliance: Alliance = findAllianceByUUID(joinKickProposalDTO.allianceId)
        for (playerInvolved in alliance.playersInvolved) {
            if (playerInvolved.id == myId)
                continue
            if (ProposalEnum.KICK == joinKickProposalDTO.proposalEnum &&
                    playerInvolved.id == joinKickProposalDTO.target)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity)
                    .sendPayload(it, streamPayload)
            }
        }
    }

    fun receiveJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        var alliance: Alliance = findAllianceByUUID(joinKickProposalDTO.allianceId)
        var targetPlayer: Player = findPlayerByUUID(joinKickProposalDTO.target)
        var initiator: Player = findPlayerByUUID(joinKickProposalDTO.initiator)
        alliance.addProposal(targetPlayer, initiator, joinKickProposalDTO.proposalId,
                                    joinKickProposalDTO.proposalEnum, 0)
    }

    fun sendMembersAction(membersAction: MembersActionDTO) {
        val data = SerializationUtils.serialize(membersAction)
        val streamPayload = Payload.zza(data, MessageCode.MEMBERS_ACTION.toLong())
        var alliance: Alliance = findAllianceByUUID(membersAction.allianceId)
        for (playerInvolved in alliance.playersInvolved) {
            if (playerInvolved.id == myId)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        if (ProposalEnum.JOIN == membersAction.proposalEnum) {
            sendAllianceDTO(alliance.convertToDTO(), membersAction.targetId)
        } else {
            acknowledgeMembersAction(membersAction)
        }
    }

    fun acknowledgeMembersAction(membersAction: MembersActionDTO) {
        var alliance: Alliance = findAllianceByUUID(membersAction.allianceId)
        var targetPlayer: Player = findPlayerByUUID(membersAction.targetId)
        if (ProposalEnum.JOIN == membersAction.proposalEnum) {
            alliance.addPlayer(targetPlayer)
        } else if (ProposalEnum.KICK == membersAction.proposalEnum) {
            alliance.kickPlayer(targetPlayer)
        }
    }

    fun sendProposalResponse(proposalResponse: ProposalResponse) {
        val data = SerializationUtils.serialize(proposalResponse)
        val streamPayload = Payload.zza(data, MessageCode.PROPOSAL_RESPONSE.toLong())
        var alliance: Alliance = findAllianceByUUID(proposalResponse.allianceId)
        var proposal: Proposal? = alliance.proposalsList.find { proposal ->
            proposal.proposalId == proposalResponse.proposalId }
        var meAsAPlayer: Player = findPlayerByUUID(myId)
        proposal?.votes?.set(meAsAPlayer, proposalResponse.response)
        idToEndpoint[proposal?.initiator?.id]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(
                it, streamPayload)
        }
    }

    fun receiveProposalResponse(proposalResponse: ProposalResponse) {
        var alliance: Alliance = findAllianceByUUID(proposalResponse.allianceId)
        var proposal: Proposal? = alliance.proposalsList.find { proposal ->
            proposal.proposalId == proposalResponse.proposalId }
        var sender: Player = findPlayerByUUID(proposalResponse.playerId)
        proposal?.registerVote(sender, proposalResponse.response)
    }

    fun sendMessage(message: ChatMessageDTO) {
        val data = SerializationUtils.serialize(message)
        val streamPayload = Payload.zza(data, MessageCode.MESSAGE_DTO.toLong())
        var alliance: Alliance = findAllianceByUUID(message.allianceId)
        for (playerInvolved in alliance.playersInvolved) {
            if (playerInvolved.id == myId)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        alliance.addMessage(message.convertToEntity())
    }

    fun receiveMessage(message: ChatMessageDTO) {
        var alliance: Alliance = findAllianceByUUID(message.allianceId)
        alliance.addMessage(message.convertToEntity())
    }

    fun acknowledgeActionEnd(actionsEndDTO: ActionEndDTO) {
        playersWithAllActionsSent.add(actionsEndDTO.playerId)
        if (playersWithAllActionsSent.size == players.value?.size) {
            // all players sent their actions, can start round end processing
            roundEndCompute()
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
        playersWithAllActionsSent.add(myId)
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
        alliance.addProposal(findPlayerByUUID(strategyProposalDTO.target),
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

    fun receiveArmyRequest(armyRequestDTO: ArmyRequestDTO) {
        var meAsAPlayer = findPlayerByUUID(armyRequestDTO.approverId)
        meAsAPlayer.armyRequestReceived.add(armyRequestDTO.convertToEntity())
    }

    fun sendArmyRequest(armyRequestDTO: ArmyRequestDTO) {
        val data = SerializationUtils.serialize(armyRequestDTO)
        val streamPayload = Payload.zza(data, MessageCode.ARMY_REQUEST.toLong())
        idToEndpoint[armyRequestDTO.approverId]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
        }
    }

    fun receiveArmyApproval(armyRequestDTO: ArmyRequestDTO) {
        var meAsAPlayer = findPlayerByUUID(armyRequestDTO.initiatorId)
        meAsAPlayer.armyImprovements.add(armyRequestDTO.convertToEntity())
    }

    fun sendArmyApproval(armyRequestDTO: ArmyRequestDTO) {
        val data = SerializationUtils.serialize(armyRequestDTO)
        val streamPayload = Payload.zza(data, MessageCode.ARMY_APPROVAL.toLong())
        idToEndpoint[armyRequestDTO.initiatorId]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
        }
    }

    fun receiveArmyAction(armyRequestDTO: ArmyRequestDTO) {
        armyActions.add(armyRequestDTO.convertToEntity())
    }

    fun sendArmyAction(armyRequestDTO: ArmyRequestDTO) {
        val data = SerializationUtils.serialize(armyRequestDTO)
        val streamPayload = Payload.zza(data, MessageCode.ARMY_UPGRADE.toLong())
        for (player in players.value!!) {
            if (player.id == myId)
                continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        armyActions.add(armyRequestDTO.convertToEntity())
    }

}