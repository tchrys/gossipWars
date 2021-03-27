package com.example.gossipwars.logic.entities

import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.MessageCode
import com.example.gossipwars.communication.messages.actions.*
import com.example.gossipwars.communication.messages.allianceCommunication.*
import com.example.gossipwars.communication.messages.gameInit.*
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.entities.GameHelper.findAllianceByUUID
import com.example.gossipwars.logic.entities.GameHelper.findPlayerByUUID
import com.example.gossipwars.logic.entities.GameHelper.findRegionDefOrAtt
import com.example.gossipwars.logic.entities.Notifications.allianceNewStructure
import com.example.gossipwars.logic.entities.Notifications.alliancesNoForMe
import com.example.gossipwars.logic.entities.Notifications.attackPropsNo
import com.example.gossipwars.logic.entities.Notifications.defensePropsNo
import com.example.gossipwars.logic.entities.Notifications.joinPropsNo
import com.example.gossipwars.logic.entities.Notifications.kickPropsNo
import com.example.gossipwars.logic.entities.Notifications.messageEmitter
import com.example.gossipwars.logic.entities.Notifications.myPropsNo
import com.example.gossipwars.logic.entities.Notifications.negotiatePropsNo
import com.example.gossipwars.logic.proposals.ArmyRequest
import com.example.gossipwars.logic.proposals.Proposal
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import org.apache.commons.lang3.SerializationUtils
import java.util.*
import java.util.logging.Handler
import kotlin.math.sqrt

object Game {
    lateinit var myId: UUID
    var endpointToId: MutableMap<String, UUID> = mutableMapOf()
    var idToEndpoint: MutableMap<UUID, String> = mutableMapOf()
    var roomInfo: RoomInfoDTO? = null
    lateinit var mainActivity: MainActivity

    var players = MutableLiveData<MutableList<Player>>().apply { value = mutableListOf() }
    var regions: MutableList<Region> = mutableListOf()
    var alliances: MutableList<Alliance> = mutableListOf()
    var regionsPerPlayers: MutableMap<Int, UUID> = mutableMapOf()
    var playersWithAllActionsSent: MutableSet<UUID> = mutableSetOf()
    var playersReadyForNewRound: MutableSet<UUID> = mutableSetOf()
    var strategyActions: MutableList<StrategyAction> = mutableListOf()
    var troopsActions: MutableList<TroopsAction> = mutableListOf()
    var armyActions: MutableList<ArmyRequest> = mutableListOf()
    var gameStarted = false

    init {
        regions = Region.initAllRegions()
    }

    fun addAlliance(player: Player, name: String): Alliance {
        val alliance: Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
        allianceNewStructure.value = true
        messageEmitter[alliance] = MutableLiveData()
        alliancesNoForMe.value = alliancesNoForMe.value?.plus(1)
        return alliance
    }

    fun simulateFight(attackers: Set<Player>, defenders: Set<Player>, region: Region) {
        val attDamage = attackers.map { player -> player.getArmyAttDamage(region.id)}.sum()
        val attSize = attackers.map { player -> player.getArmySizeForRegion(region.id) }.sum()
        val defDamage = defenders.map { player -> player.getArmyDefDamage(region.id) }.sum()
        val defSize = defenders.map { player -> player.getArmySizeForRegion(region.id) }.sum()
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
//        for (i in 0..100000000) {
//            sqrt(24.43)
//        }
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
        sendStartRound(StartRoundDTO(myId))
    }

    fun sendMyInfo() {
        val meAsAPlayer = players.value?.get(0)
        val myPlayerDTO = meAsAPlayer?.username?.let {
            PlayerDTO(
                it,
                meAsAPlayer.id
            )
        }
        val data = SerializationUtils.serialize(myPlayerDTO)
        val streamPayload = Payload.zza(data, MessageCode.PLAYER_INFO.toLong())
        for (playerEndpoint in mainActivity.peers) {
            Nearby.getConnectionsClient(mainActivity).sendPayload(playerEndpoint, streamPayload)
        }
        if (mainActivity.peers.size == 0) {
            sendOrderPayload()
        }
    }

    fun acknowledgePlayer(playerDTO: PlayerDTO, endpointId: String) {
        if (players.value?.find { player -> player.id == playerDTO.id } == null) {
            players.value?.add(Player(playerDTO.username, playerDTO.id))
            endpointToId[endpointId] = playerDTO.id
            idToEndpoint[playerDTO.id] = endpointId
            if (roomInfo?.username == players.value?.get(0)?.username &&
                roomInfo?.crtPlayersNr == players.value?.size
            ) {
                sendOrderPayload()
            }
        }
    }

    private fun sendOrderPayload() {
        val ans = mutableListOf<PlayerWithOrderDTO>()
        players.value?.forEachIndexed { index, player ->
            ans.add(
                PlayerWithOrderDTO(
                    index,
                    player.username,
                    player.id
                )
            )
        }
        val playersOrderDTO =
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
        val playersIds = players.value?.map { player -> player.id }
        players.value?.forEachIndexed { index, player ->
            regionsPerPlayers[index] = player.id
            player.army = Army.initDefaultArmy(regions[index])
            player.winRegion(regions[index])
            if (player.id == myId) {
                regions.forEach { region: Region ->
                    player.soldiersUsedThisRound[region.id] = 0 }
                if (playersIds != null) {
                    for (playerId in playersIds) {
                        if (playerId != myId) {
                            player.trustInOthers[playerId] = 5
                        }
                    }
                }
            }
        }
        Notifications.roundTimer.value = roomInfo?.roundLength
        Notifications.createTimeCounter()
        gameStarted = true
    }

    fun sendAllianceDTO(allianceInvitationDTO: AllianceInvitationDTO, targetId: UUID) {
        val data = SerializationUtils.serialize(allianceInvitationDTO)
        val streamPayload = Payload.zza(data, MessageCode.ALLIANCE_INFO.toLong())
        idToEndpoint[targetId]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
        }
        // register new member
        val alliance: Alliance = findAllianceByUUID(allianceInvitationDTO.id)
        val player: Player = findPlayerByUUID(targetId)
        alliance.addPlayer(player)
        allianceNewStructure.value = true
    }

    fun receiveNewAllianceInfo(allianceInvitationDTO: AllianceInvitationDTO) {
        val alliance: Alliance = allianceInvitationDTO.convertToEntity()
        alliances.add(alliance)
        alliance.addPlayer(findPlayerByUUID(myId))
        allianceNewStructure.value = true
        alliancesNoForMe.value = alliancesNoForMe.value?.plus(1)
        messageEmitter[alliance] = MutableLiveData()
    }

    fun sendJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        val data = SerializationUtils.serialize(joinKickProposalDTO)
        val streamPayload = Payload.zza(data, MessageCode.JOIN_KICK_PROPOSAL.toLong())
        val alliance: Alliance = findAllianceByUUID(joinKickProposalDTO.allianceId)
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
        myPropsNo.value = myPropsNo.value?.plus(1)
    }

    fun receiveJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        val alliance: Alliance = findAllianceByUUID(joinKickProposalDTO.allianceId)
        val targetPlayer: Player = findPlayerByUUID(joinKickProposalDTO.target)
        val initiator: Player = findPlayerByUUID(joinKickProposalDTO.initiator)
        alliance.addProposal(targetPlayer, initiator, joinKickProposalDTO.proposalId,
                                    joinKickProposalDTO.proposalEnum, 0)
        if (joinKickProposalDTO.proposalEnum == ProposalEnum.JOIN)
            joinPropsNo.value = joinPropsNo.value?.plus(1)
        else
            kickPropsNo.value = kickPropsNo.value?.plus(1)
    }

    fun sendMembersAction(membersAction: MembersActionDTO) {
        val data = SerializationUtils.serialize(membersAction)
        val streamPayload = Payload.zza(data, MessageCode.MEMBERS_ACTION.toLong())
        val alliance: Alliance = findAllianceByUUID(membersAction.allianceId)
        for (playerInvolved in alliance.playersInvolved) {
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
        val alliance: Alliance = findAllianceByUUID(membersAction.allianceId)
        val targetPlayer: Player = findPlayerByUUID(membersAction.targetId)
        if (ProposalEnum.JOIN == membersAction.proposalEnum) {
            alliance.addPlayer(targetPlayer)
            if (targetPlayer.id == myId)
                alliancesNoForMe.value = alliancesNoForMe.value?.plus(1)
        } else if (ProposalEnum.KICK == membersAction.proposalEnum) {
            alliance.kickPlayer(targetPlayer)
            if (targetPlayer.id == myId)
                alliancesNoForMe.value = alliancesNoForMe.value?.minus(1)
        }
        allianceNewStructure.value = true
    }

    fun sendProposalResponse(proposalResponse: ProposalResponse) {
        val data = SerializationUtils.serialize(proposalResponse)
        val streamPayload = Payload.zza(data, MessageCode.PROPOSAL_RESPONSE.toLong())
        val alliance: Alliance = findAllianceByUUID(proposalResponse.allianceId)
        val proposal: Proposal? = alliance.proposalsList.find { proposal ->
            proposal.proposalId == proposalResponse.proposalId }
        val meAsAPlayer: Player = findPlayerByUUID(myId)
        proposal?.votes?.set(meAsAPlayer, proposalResponse.response)
        idToEndpoint[proposal?.initiator?.id]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(
                it, streamPayload)
        }
    }

    fun receiveProposalResponse(proposalResponse: ProposalResponse) {
        val alliance: Alliance = findAllianceByUUID(proposalResponse.allianceId)
        val proposal: Proposal? = alliance.proposalsList.find { proposal ->
            proposal.proposalId == proposalResponse.proposalId }
        val sender: Player = findPlayerByUUID(proposalResponse.playerId)
        proposal?.registerVote(sender, proposalResponse.response)
    }

    fun sendMessage(message: ChatMessageDTO) {
        val data = SerializationUtils.serialize(message)
        val streamPayload = Payload.zza(data, MessageCode.MESSAGE_DTO.toLong())
        val alliance: Alliance = findAllianceByUUID(message.allianceId)
        for (playerInvolved in alliance.playersInvolved) {
            if (playerInvolved.id == myId)
                continue
            idToEndpoint[playerInvolved.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        alliance.addMessage(message.convertToEntity())
        messageEmitter[alliance]?.value = message.convertToEntity()
    }

    fun receiveMessage(message: ChatMessageDTO) {
        val alliance: Alliance = findAllianceByUUID(message.allianceId)
        alliance.addMessage(message.convertToEntity())
        messageEmitter[alliance]?.value = message.convertToEntity()
    }

    fun acknowledgeActionEnd(actionsEndDTO: ActionEndDTO) {
        playersWithAllActionsSent.add(actionsEndDTO.playerId)
        if (playersWithAllActionsSent.size == players.value?.size) {
            // all players sent their actions, can start round end processing
            roundEndCompute()
        }
    }

    fun sendActionEnd(actionsEndDTO: ActionEndDTO) {
        Notifications.roundOngoing.value = false
        val data = SerializationUtils.serialize(actionsEndDTO)
        val streamPayload = Payload.zza(data, MessageCode.ACTION_END.toLong())
        for (player in players.value!!) {
            if (player.id == myId) continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        playersWithAllActionsSent.add(myId)
        if (playersWithAllActionsSent.size == players.value?.size) {
            roundEndCompute()
        }
    }

    fun receiveStrategyAction(strategyActionDTO: StrategyActionDTO) {
        strategyActions.add(strategyActionDTO.convertToStrategyAction())
    }

    fun sendStrategyAction(strategyActionDTO: StrategyActionDTO) {
        val data = SerializationUtils.serialize(strategyActionDTO)
        val streamPayload = Payload.zza(data, MessageCode.STRATEGY_ACTION.toLong())
        for (player in players.value!!) {
            if (player.id == myId)
                continue
            idToEndpoint[player.id]?.let {
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
        if (strategyProposalDTO.proposalEnum == ProposalEnum.ATTACK)
            attackPropsNo.value = attackPropsNo.value?.plus(1)
        else
            defensePropsNo.value = defensePropsNo.value?.plus(1)
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
        myPropsNo.value = myPropsNo.value?.plus(1)
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
        val meAsAPlayer = findPlayerByUUID(armyRequestDTO.approverId)
        meAsAPlayer.armyRequestReceived.add(armyRequestDTO.convertToEntity())
        negotiatePropsNo.value = negotiatePropsNo.value?.plus(1)
    }

    fun sendArmyRequest(armyRequestDTO: ArmyRequestDTO) {
        val data = SerializationUtils.serialize(armyRequestDTO)
        val streamPayload = Payload.zza(data, MessageCode.ARMY_REQUEST.toLong())
        idToEndpoint[armyRequestDTO.approverId]?.let {
            Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
        }
    }

    fun receiveArmyApproval(armyRequestDTO: ArmyRequestDTO) {
        sendArmyAction(armyRequestDTO)
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

    fun sendStartRound(startRoundDTO: StartRoundDTO) {
        val data = SerializationUtils.serialize(startRoundDTO)
        val streamPayload = Payload.zza(data, MessageCode.START_ROUND.toLong())
        for (player in players.value!!) {
            if (player.id == myId) continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        playersReadyForNewRound.add(myId)
        if (playersReadyForNewRound.size == players.value?.size)
            Notifications.roundOngoing.value = true
    }

    fun acknowledgeStartRound(startRoundDTO: StartRoundDTO) {
        playersReadyForNewRound.add(startRoundDTO.playerId)
        if (playersReadyForNewRound.size == players.value?.size) {
            // all players are ready to start a new round
            Notifications.roundOngoing.value = true
        }
    }

}