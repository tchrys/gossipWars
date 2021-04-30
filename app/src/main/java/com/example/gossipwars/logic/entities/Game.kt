package com.example.gossipwars.logic.entities

import androidx.lifecycle.MutableLiveData
import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.MessageCode
import com.example.gossipwars.communication.messages.actions.*
import com.example.gossipwars.communication.messages.allianceCommunication.*
import com.example.gossipwars.communication.messages.gameInit.*
import com.example.gossipwars.logic.actions.MembersAction
import com.example.gossipwars.logic.actions.StrategyAction
import com.example.gossipwars.logic.actions.TroopsAction
import com.example.gossipwars.logic.entities.GameHelper.findAllianceByUUID
import com.example.gossipwars.logic.entities.GameHelper.findPlayerByUUID
import com.example.gossipwars.logic.entities.GameHelper.findRegionAttackInitiator
import com.example.gossipwars.logic.entities.GameHelper.findRegionDefOrAtt
import com.example.gossipwars.logic.entities.GameHelper.findRegionOwner
import com.example.gossipwars.logic.entities.GameHelper.regionDominantArmy
import com.example.gossipwars.logic.entities.GameHelper.soldiersForRegion
import com.example.gossipwars.logic.entities.Notifications.allianceNewStructure
import com.example.gossipwars.logic.entities.Notifications.alliancesNoForMe
import com.example.gossipwars.logic.entities.Notifications.attackPropsNo
import com.example.gossipwars.logic.entities.Notifications.crtRoundNo
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.SerializationUtils
import java.util.*

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

    fun gameEndCleanup() {
        players.value?.clear()
        alliances.clear()
        regionsPerPlayers.clear()
        playersWithAllActionsSent.clear()
        playersReadyForNewRound.clear()
        strategyActions.clear()
        troopsActions.clear()
        armyActions.clear()
        gameStarted = false

        endpointToId.clear()
        idToEndpoint.clear()
        roomInfo = null

        Snapshots.cleanup()
        Notifications.cleanup()
        mainActivity.cleanup()
    }

    fun addAlliance(player: Player, name: String): Alliance {
        val alliance: Alliance = Alliance.initAlliance(player, name)
        alliances.add(alliance)
        allianceNewStructure.value = true
        messageEmitter[alliance.id] = MutableLiveData()
        alliancesNoForMe.value = alliancesNoForMe.value?.plus(1)
        return alliance
    }

    fun simulateFight(attackers: Set<Player>, defenders: Set<Player>, region: Region) {
        val attDamage = attackers.map { player -> player.getArmyAttDamage(region.id) }.sum()
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
        // if this territory was lost
        val owner: Player? = findRegionOwner(region.id)
        if (owner != null && attackers.isNotEmpty()) {
            val soldiersLeft: Int? = soldiersForRegion(region.name, owner.id)
            if (soldiersLeft == null || soldiersLeft == 0) {
                val attackInitiator: Player? = findRegionAttackInitiator(region.id)
                owner.loseRegion(region)
                attackInitiator?.winRegion(region)
                checkLoserCapital(owner, region)
            }
        }
    }

    private fun checkLoserCapital(player: Player, region: Region) {
        if (player.capitalRegion == region.id) {
            if (player.regionsOccupied.isEmpty()) {
                player.capitalRegion = -1
            } else {
                player.capitalRegion = player.regionsOccupied.first().id
            }
        }
    }

    fun roundEndCompute() {

        Snapshots.armyImprovementsPerRound.add(mutableListOf())
        Snapshots.fightsPerRound.add(mutableListOf())
        Snapshots.troopsMovedPerRound.add(mutableListOf())
        for (region in regions) {
//            attackers = GlobalScope.async {find...} apoi await()
            val attackers: Set<Player> = findRegionDefOrAtt(region.id, ProposalEnum.ATTACK)
            val defenders: Set<Player> = findRegionDefOrAtt(region.id, ProposalEnum.DEFEND)
            Snapshots.fightsPerRound[crtRoundNo.value!!].add(Fight(attackers, defenders, region))
            simulateFight(attackers, defenders, region)
        }
        // troop actions
        for (troopAction in troopsActions) {
            troopAction.initiator.moveTroops(
                troopAction.fromRegion,
                troopAction.toRegion,
                troopAction.size
            )
            Snapshots.troopsMovedPerRound[crtRoundNo.value!!].add(troopAction)
        }
        // army actions
        for (armyAction in armyActions) {
            armyAction.initiator.improveArmy(armyAction)
            Snapshots.armyImprovementsPerRound[crtRoundNo.value!!].add(armyAction)
        }
        // compute army size for players
        players.value?.forEach { player: Player -> player.computeArmySize() }
        // cleanup and start new round
        doCleanup()
        sendStartRound(StartRoundDTO(myId))
    }

    private fun doCleanup() {
        val meAsAPlayer = findPlayerByUUID(myId)
        meAsAPlayer.soldiersUsedThisRound.clear()
        meAsAPlayer.armyRequestReceived.clear()
        strategyActions.clear()
        troopsActions.clear()
        armyActions.clear()
        alliances.forEach { alliance: Alliance ->
            alliance.proposalsList.clear()
        }
        negotiatePropsNo.postValue(0)
        defensePropsNo.postValue(0)
        attackPropsNo.postValue(0)
        joinPropsNo.postValue(0)
        kickPropsNo.postValue(0)
        myPropsNo.postValue(0)

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
        if (roomInfo?.crtPlayersNr == 1) {
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
        val playersNo: Int = players.value?.size!!
        val regionsNo: Int = regions.size
        val regionsPerPlayer = regionsNo / playersNo
        players.value?.forEachIndexed { index, player ->
            val floor = index * regionsPerPlayer
            player.army = Army.initDefaultArmy(regions[floor])
            var ceil = (index + 1) * regionsPerPlayer
            if (index == players.value?.size!! - 1)
                ceil = regions.size
            for (i in floor until ceil) {
                regionsPerPlayers[i] = player.id
                player.winRegion(regions[i])
            }
            player.capitalRegion = regions[floor].id

            if (player.id == myId) {
                regions.forEach { region: Region ->
                    player.soldiersUsedThisRound[region.id] = 0
                }
                if (playersIds != null) {
                    for (playerId in playersIds) {
                        if (playerId != myId) {
                            player.trustInOthers[playerId] = 5
                        }
                    }
                }
            }
        }
        Notifications.roundTimer.postValue(roomInfo?.roundLength)
        Notifications.roundOngoing.postValue(true)
        Notifications.myBonusTaken.postValue(false)
        crtRoundNo.postValue(0)

        Notifications.createTimeCounter()
        gameStarted = true
    }

    suspend fun sendAllianceDTO(allianceInvitationDTO: AllianceInvitationDTO, targetId: UUID) {
        withContext(Dispatchers.IO) {
            val data = SerializationUtils.serialize(allianceInvitationDTO)
            val streamPayload = Payload.zza(data, MessageCode.ALLIANCE_INFO.toLong())
            idToEndpoint[targetId]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
            // register new member
            val alliance: Alliance = findAllianceByUUID(allianceInvitationDTO.id)
            val player: Player = findPlayerByUUID(targetId)
            alliance.addPlayer(player)
            allianceNewStructure.postValue(true)
        }
    }

    suspend fun receiveNewAllianceInfo(allianceInvitationDTO: AllianceInvitationDTO) {
        withContext(Dispatchers.Default) {
            val alliance: Alliance = allianceInvitationDTO.convertToEntity()
            alliances.add(alliance)
            alliance.addPlayer(findPlayerByUUID(myId))
            allianceNewStructure.postValue(true)
            alliancesNoForMe.postValue(alliancesNoForMe.value?.plus(1))
            messageEmitter[alliance.id] = MutableLiveData()
        }
    }

    suspend fun sendJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        withContext(Dispatchers.IO) {
            val data = SerializationUtils.serialize(joinKickProposalDTO)
            val streamPayload = Payload.zza(data, MessageCode.JOIN_KICK_PROPOSAL.toLong())
            val alliance: Alliance = findAllianceByUUID(joinKickProposalDTO.allianceId)
            for (playerInvolved in alliance.playersInvolved) {
                if (playerInvolved.id == myId)
                    continue
                if (ProposalEnum.KICK == joinKickProposalDTO.proposalEnum &&
                    playerInvolved.id == joinKickProposalDTO.target
                )
                    continue
                idToEndpoint[playerInvolved.id]?.let {
                    Nearby.getConnectionsClient(mainActivity)
                        .sendPayload(it, streamPayload)
                }
            }
            myPropsNo.postValue(myPropsNo.value?.plus(1))
        }
    }

    suspend fun receiveJoinKickProposalDTO(joinKickProposalDTO: JoinKickProposalDTO) {
        withContext(Dispatchers.Default) {
            val alliance: Alliance = findAllianceByUUID(joinKickProposalDTO.allianceId)
            val targetPlayer: Player = findPlayerByUUID(joinKickProposalDTO.target)
            val initiator: Player = findPlayerByUUID(joinKickProposalDTO.initiator)
            alliance.addProposal(
                targetPlayer, initiator, joinKickProposalDTO.proposalId,
                joinKickProposalDTO.proposalEnum, 0
            )
            if (joinKickProposalDTO.proposalEnum == ProposalEnum.JOIN)
                joinPropsNo.postValue(joinPropsNo.value?.plus(1))
            else
                kickPropsNo.postValue(kickPropsNo.value?.plus(1))
        }
    }

    suspend fun sendMembersAction(membersAction: MembersAction) {
        withContext(Dispatchers.IO) {
            val data = SerializationUtils.serialize(membersAction.convertToDTO())
            val streamPayload = Payload.zza(data, MessageCode.MEMBERS_ACTION.toLong())
            val alliance: Alliance = findAllianceByUUID(membersAction.alliance.id)
            for (playerInvolved in alliance.playersInvolved) {
                if (playerInvolved.id == myId)
                    continue
                idToEndpoint[playerInvolved.id]?.let {
                    Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
                }
            }
            if (ProposalEnum.JOIN == membersAction.proposalEnum) {
                sendAllianceDTO(alliance.convertToDTO(), membersAction.target.id)
            }
            acknowledgeMembersAction(membersAction.convertToDTO())
        }
    }

    suspend fun acknowledgeMembersAction(membersAction: MembersActionDTO) {
        withContext(Dispatchers.Default) {
            val alliance: Alliance = findAllianceByUUID(membersAction.allianceId)
            val targetPlayer: Player = findPlayerByUUID(membersAction.targetId)
            if (ProposalEnum.JOIN == membersAction.proposalEnum) {
                alliance.addPlayer(targetPlayer)
                if (targetPlayer.id == myId)
                    alliancesNoForMe.postValue(alliancesNoForMe.value?.plus(1))
            } else if (ProposalEnum.KICK == membersAction.proposalEnum) {
                alliance.kickPlayer(targetPlayer)
                if (targetPlayer.id == myId) {
                    alliancesNoForMe.postValue(alliancesNoForMe.value?.minus(1))
                    alliances.remove(alliance)
                } else if (alliance.playersInvolved.size < 2) {
                    alliance.kickPlayer(findPlayerByUUID(myId))
                    alliances.remove(alliance)
                    alliancesNoForMe.postValue(alliancesNoForMe.value?.minus(1))
                }

            }
            allianceNewStructure.postValue(true)
        }
    }

    suspend fun sendProposalResponse(proposalResponse: ProposalResponse) {
        withContext(Dispatchers.IO) {
            val data = SerializationUtils.serialize(proposalResponse)
            val streamPayload = Payload.zza(data, MessageCode.PROPOSAL_RESPONSE.toLong())
            val alliance: Alliance = findAllianceByUUID(proposalResponse.allianceId)
            val proposal: Proposal? = alliance.proposalsList.find { proposal ->
                proposal.proposalId == proposalResponse.proposalId
            }
            val meAsAPlayer: Player = findPlayerByUUID(myId)
            proposal?.votes?.set(meAsAPlayer, proposalResponse.response)
            idToEndpoint[proposal?.initiator?.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(
                    it, streamPayload
                )
            }
        }
    }

    suspend fun receiveProposalResponse(proposalResponse: ProposalResponse) {
        withContext(Dispatchers.Default) {
            val alliance: Alliance = findAllianceByUUID(proposalResponse.allianceId)
            val proposal: Proposal? = alliance.proposalsList.find { proposal ->
                proposal.proposalId == proposalResponse.proposalId
            }
            val sender: Player = findPlayerByUUID(proposalResponse.playerId)
            proposal?.registerVote(sender, proposalResponse.response)
        }
    }

    suspend fun sendMessage(message: ChatMessageDTO) {
        withContext(Dispatchers.IO) {
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
            messageEmitter[alliance.id]?.postValue(message.convertToEntity())
        }
    }

    suspend fun receiveMessage(message: ChatMessageDTO) {
        withContext(Dispatchers.Default) {
            val alliance: Alliance = findAllianceByUUID(message.allianceId)
            alliance.addMessage(message.convertToEntity())
            messageEmitter[alliance.id]?.postValue(message.convertToEntity())
            alliance.messagesSeen = false
        }
    }

    suspend fun acknowledgeActionEnd(actionsEndDTO: ActionEndDTO) {
        withContext(Dispatchers.Default) {
            playersWithAllActionsSent.add(actionsEndDTO.playerId)
            if (playersWithAllActionsSent.size == players.value?.size) {
                // all players sent their actions, can start round end processing
                roundEndCompute()
            }
        }
    }

    fun sendActionEnd(actionsEndDTO: ActionEndDTO) {
        Notifications.roundOngoing.postValue(false)
        sendStrategyActions()
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

    suspend fun receiveStrategyAction(strategyActionDTO: StrategyActionDTO) {
        withContext(Dispatchers.Default) {
            strategyActions.add(strategyActionDTO.convertToEntity())
        }
    }

    fun sendStrategyAction(strategyAction: StrategyAction) {
        val data = SerializationUtils.serialize(strategyAction.convertToDTO())
        val streamPayload = Payload.zza(data, MessageCode.STRATEGY_ACTION.toLong())
        for (player in players.value!!) {
            if (player.id == myId)
                continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        strategyActions.add(strategyAction)
    }

    private fun sendStrategyActions() {
        GameHelper.findMyProposals()?.filter { proposal -> !proposal.isMemberProposal() }
            ?.forEach { proposal: Proposal -> sendStrategyAction(proposal.createAction() as StrategyAction) }
    }

    suspend fun receiveStrategyProposal(strategyProposalDTO: StrategyProposalDTO) {
        withContext(Dispatchers.Default) {
            val alliance: Alliance = findAllianceByUUID(strategyProposalDTO.allianceId)
            alliance.addProposal(
                findPlayerByUUID(strategyProposalDTO.target),
                findPlayerByUUID(strategyProposalDTO.initiator),
                strategyProposalDTO.proposalId, strategyProposalDTO.proposalEnum,
                strategyProposalDTO.targetRegion
            )
            if (strategyProposalDTO.proposalEnum == ProposalEnum.ATTACK)
                attackPropsNo.postValue(attackPropsNo.value?.plus(1))
            else
                defensePropsNo.postValue(defensePropsNo.value?.plus(1))
        }
    }

    suspend fun sendStrategyProposal(strategyProposalDTO: StrategyProposalDTO) {
        withContext(Dispatchers.IO) {
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
            myPropsNo.postValue(myPropsNo.value?.plus(1))
        }
    }

    suspend fun receiveTroopsAction(troopsActionDTO: TroopsActionDTO) {
        withContext(Dispatchers.Default) {
            troopsActions.add(troopsActionDTO.convertToEntity())
        }
    }

    suspend fun sendTroopsAction(troopsActionDTO: TroopsActionDTO) {
        withContext(Dispatchers.IO) {
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

    suspend fun receiveArmyRequest(armyRequestDTO: ArmyRequestDTO) {
        withContext(Dispatchers.Default) {
            val meAsAPlayer = findPlayerByUUID(armyRequestDTO.approverId)
            meAsAPlayer.armyRequestReceived.add(armyRequestDTO.convertToEntity())
            negotiatePropsNo.postValue(negotiatePropsNo.value?.plus(1))
        }
    }


    suspend fun sendArmyRequest(armyRequestDTO: ArmyRequestDTO) {
        withContext(Dispatchers.IO) {
            val data = SerializationUtils.serialize(armyRequestDTO)
            val streamPayload = Payload.zza(data, MessageCode.ARMY_REQUEST.toLong())
            idToEndpoint[armyRequestDTO.approverId]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
    }

    suspend fun receiveArmyApproval(armyRequestDTO: ArmyRequestDTO) {
        sendArmyAction(armyRequestDTO)
    }

    suspend fun sendArmyApproval(armyRequestDTO: ArmyRequestDTO) {
        withContext(Dispatchers.IO) {
            val data = SerializationUtils.serialize(armyRequestDTO)
            val streamPayload = Payload.zza(data, MessageCode.ARMY_APPROVAL.toLong())
            idToEndpoint[armyRequestDTO.initiatorId]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
    }

    suspend fun receiveArmyAction(armyRequestDTO: ArmyRequestDTO) {
        withContext(Dispatchers.Default) {
            armyActions.add(armyRequestDTO.convertToEntity())
        }
    }

    suspend fun sendArmyAction(armyRequestDTO: ArmyRequestDTO) {
        withContext(Dispatchers.IO) {
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

    private fun sendStartRound(startRoundDTO: StartRoundDTO) {
        val data = SerializationUtils.serialize(startRoundDTO)
        val streamPayload = Payload.zza(data, MessageCode.START_ROUND.toLong())
        for (player in players.value!!) {
            if (player.id == myId) continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        playersReadyForNewRound.add(myId)
        if (playersReadyForNewRound.size == players.value?.size) {
            Notifications.roundOngoing.postValue(true)
            Notifications.myBonusTaken.postValue(false)
            crtRoundNo.postValue(crtRoundNo.value?.plus(1))
            playersReadyForNewRound.clear()
        }
    }

    suspend fun acknowledgeStartRound(startRoundDTO: StartRoundDTO) {
        withContext(Dispatchers.Default) {
            playersReadyForNewRound.add(startRoundDTO.playerId)
            if (playersReadyForNewRound.size == players.value?.size) {
                // all players are ready to start a new round
                Notifications.roundOngoing.postValue(true)
                Notifications.myBonusTaken.postValue(false)
                crtRoundNo.postValue(crtRoundNo.value?.plus(1))
                playersReadyForNewRound.clear()
            }
        }
    }

    fun sendSurrender() {
        val data = SerializationUtils.serialize(ActionEndDTO(myId))
        val streamPayload = Payload.zza(data, MessageCode.SURRENDER.toLong())
        for (player in players.value!!) {
            if (player.id == myId) continue
            idToEndpoint[player.id]?.let {
                Nearby.getConnectionsClient(mainActivity).sendPayload(it, streamPayload)
            }
        }
        gameEndCleanup()
    }

    suspend fun acknowledgePlayerSurrender(actionsEndDTO: ActionEndDTO) {
        withContext(Dispatchers.Default) {
            val player: Player? =
                players.value?.find { player -> player.id == actionsEndDTO.playerId }
            player?.regionsOccupied?.forEach { region: Region ->
                val regionWinner: Player? = regionDominantArmy(region.name, player.id)
                regionWinner?.winRegion(region)
                if (regionWinner?.capitalRegion == -1) {
                    regionWinner.capitalRegion = region.id
                }
            }
            alliances.forEach { alliance: Alliance ->
                val idx = alliance.playersInvolved.indexOfFirst { player -> player.id == player.id }
                if (idx != -1)
                    alliance.playersInvolved.removeAt(idx)
            }

            players.value?.remove(player)
//            val playerIdx: Int? =
//                players.value?.indexOfFirst { p -> p.id == actionsEndDTO.playerId }
//            if (playerIdx != null && playerIdx != -1)
//                players.value?.removeAt(playerIdx)
            players.postValue(players.value)
        }
    }

}