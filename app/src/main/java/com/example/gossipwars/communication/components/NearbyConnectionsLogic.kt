package com.example.gossipwars.communication.components

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.*
import com.example.gossipwars.communication.messages.actions.*
import com.example.gossipwars.communication.messages.allianceCommunication.*
import com.example.gossipwars.communication.messages.gameInit.PlayerDTO
import com.example.gossipwars.communication.messages.gameInit.PlayersOrderDTO
import com.example.gossipwars.communication.messages.gameInit.RoomInfoDTO
import com.example.gossipwars.communication.messages.gameInit.StartRoundDTO
import com.example.gossipwars.logic.entities.Game
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.SerializationUtils

class NearbyConnectionsLogic(val mainActivity: MainActivity) {

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
             override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // an endpoint was found. we request a connection to it
                Nearby.getConnectionsClient(mainActivity)
                    .requestConnection(mainActivity.username.orEmpty(), endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener {
                        // we successfully requested a connection. now both sides
                        // must accept before the connection is established
                    }
                    .addOnFailureListener {
                        // nearby connections failed to request the connection
                    }
            }

            override fun onEndpointLost(endpointId: String) {
                // a previously discovered endpoint has gone away
            }
        }

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        if (mainActivity.acceptedUsers.contains(endpointId)) {
                            mainActivity.peers.add(endpointId)
                        }
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.
            }

            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                AlertDialog.Builder(mainActivity)
                    .setTitle("Accept connection to ${connectionInfo.endpointName}")
                    .setMessage("Confirm the code matches on both devices ${connectionInfo.authenticationToken}")
                    .setPositiveButton("Accept") { _, _ ->
                        // the user confirmed, so we can accept the connection
                        Nearby.getConnectionsClient(mainActivity)
                            .acceptConnection(endpointId, payloadCallback)
                        mainActivity.acceptedUsers.add(endpointId)
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // the user canceled, so we should reject the connection
                        Nearby.getConnectionsClient(mainActivity).rejectConnection(endpointId)
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }

    suspend fun startAdvertising() {
        withContext(Dispatchers.IO) {
            val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
            Nearby.getConnectionsClient(mainActivity)
                .startAdvertising(mainActivity.username.orEmpty(), "com.example.gossipwars",
                    connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener {
                    Log.d("DBG", "start advertising")
                }
                .addOnFailureListener {
                }
        }
    }

    suspend fun startDiscovery() {
        withContext(Dispatchers.IO) {
            val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
            Nearby.getConnectionsClient(mainActivity).startDiscovery("com.example.gossipwars",
                endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener {
                    Log.d("DBG", "start discovering")
                }
                .addOnFailureListener {
                }
        }
    }

    private val payloadCallback: PayloadCallback =
        object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                // This always gets the full data of the payload. Will be null if it's not a BYTES
                // payload. You can check the payload type with payload.getType().
                val receivedBytes = payload.asBytes()
                when (payload.id) {
                    MessageCode.ROOM_INFO.toLong() -> {
                        val roomInfo: RoomInfoDTO = SerializationUtils.deserialize(receivedBytes)
                        mainActivity.manageRoomInfoPayload(roomInfo, endpointId)
                    }
                    MessageCode.ALLIANCE_INFO.toLong() -> {
                        val allianceInvitationDTO: AllianceInvitationDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveNewAllianceInfo(allianceInvitationDTO) }
                    }
                    MessageCode.JOIN_KICK_PROPOSAL.toLong() -> {
                        val joinKickProposalDTO: JoinKickProposalDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveJoinKickProposalDTO(joinKickProposalDTO) }
                    }
                    MessageCode.MEMBERS_ACTION.toLong() -> {
                        val membersAction: MembersActionDTO = SerializationUtils.deserialize(receivedBytes)
                        Game.acknowledgeMembersAction(membersAction)
                    }
                    MessageCode.MESSAGE_DTO.toLong() -> {
                        val messageDTO: ChatMessageDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveMessage(messageDTO) }
                    }
                    MessageCode.PLAYER_INFO.toLong() -> {
                        val playerDTO: PlayerDTO = SerializationUtils.deserialize(receivedBytes)
                        Game.acknowledgePlayer(playerDTO, endpointId)
                    }
                    MessageCode.ACTION_END.toLong() -> {
                        val actionsEndDTO: ActionEndDTO = SerializationUtils.deserialize(receivedBytes)
                        Game.acknowledgeActionEnd(actionsEndDTO)
                    }
                    MessageCode.SURRENDER.toLong() -> {
                        val surrenderDTO: ActionEndDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.acknowledgePlayerSurrender(surrenderDTO) }
                    }
                    MessageCode.PROPOSAL_RESPONSE.toLong() -> {
                        val proposalResponse: ProposalResponse = SerializationUtils.deserialize(receivedBytes)
                        Game.receiveProposalResponse(proposalResponse)
                    }
                    MessageCode.STRATEGY_ACTION.toLong() -> {
                        val strategyActionDTO: StrategyActionDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveStrategyAction(strategyActionDTO) }
                    }
                    MessageCode.STRATEGY_PROPOSAL.toLong() -> {
                        val strategyProposalDTO: StrategyProposalDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveStrategyProposal(strategyProposalDTO) }
                    }
                    MessageCode.TROOPS_ACTION.toLong() -> {
                        val troopsActionDTO: TroopsActionDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveTroopsAction(troopsActionDTO) }
                    }
                    MessageCode.PLAYER_ORDER.toLong() -> {
                        val playersOrderDTO: PlayersOrderDTO = SerializationUtils.deserialize(receivedBytes)
                        Game.reorderPlayers(playersOrderDTO)
                    }
                    MessageCode.ARMY_REQUEST.toLong() -> {
                        val armyRequestDTO: ArmyRequestDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveArmyRequest(armyRequestDTO) }
                    }
                    MessageCode.ARMY_APPROVAL.toLong() -> {
                        val armyRequestDTO: ArmyRequestDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveArmyApproval(armyRequestDTO) }
                    }
                    MessageCode.ARMY_UPGRADE.toLong() -> {
                        val armyRequestDTO: ArmyRequestDTO = SerializationUtils.deserialize(receivedBytes)
                        GlobalScope.launch { Game.receiveArmyAction(armyRequestDTO) }
                    }
                    MessageCode.START_ROUND.toLong() -> {
                        val startRoundDTO: StartRoundDTO = SerializationUtils.deserialize(receivedBytes)
                        Game.acknowledgeStartRound(startRoundDTO)
                    }
                }
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
                // after the call to onPayloadReceived().
            }
        }

}