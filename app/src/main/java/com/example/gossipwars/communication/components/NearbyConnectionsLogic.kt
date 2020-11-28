package com.example.gossipwars.communication.components

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.gossipwars.MainActivity
import com.example.gossipwars.communication.messages.MessageCode
import com.example.gossipwars.communication.messages.RoomInfo
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import org.apache.commons.lang3.SerializationUtils

class NearbyConnectionsLogic(val mainActivity: MainActivity) {

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // an endpoint was found. we request a connection to it
                Nearby.getConnectionsClient(mainActivity)
                    .requestConnection(mainActivity.username.orEmpty(), endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener { void ->
                        // we successfully requested a connection. now both sides
                        // must accept before the connection is established
                        Toast.makeText(mainActivity, "Request connection", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { exception ->
                        // nearby connections failed to request the connection
                        Toast.makeText(mainActivity, "Request failed", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(mainActivity, "Status ok", Toast.LENGTH_LONG).show()
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
                    .setTitle("Accept connection to " + connectionInfo.endpointName)
                    .setMessage("Confirm the code matches on both devices " + connectionInfo.authenticationToken)
                    .setPositiveButton("Accept") {
                            dialogInterface, i ->
                        // the user confirmed, so we can accept the connection
                        Nearby.getConnectionsClient(mainActivity)
                            .acceptConnection(endpointId, payloadCallback)
                        mainActivity.acceptedUsers.add(endpointId)
                        Toast.makeText(mainActivity, endpointId, Toast.LENGTH_LONG).show()
                    }
                    .setNegativeButton("Cancel") {
                            dialogInterface, i ->
                        // the user canceled, so we should reject the connection
                        Nearby.getConnectionsClient(mainActivity).rejectConnection(endpointId)
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        Nearby.getConnectionsClient(mainActivity)
            .startAdvertising(mainActivity.username.orEmpty(), "com.example.gossipwars",
                connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener { void ->
                Toast.makeText(mainActivity, "We are advertising", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(mainActivity, "Unable to advertise", Toast.LENGTH_LONG).show()
            }
    }

    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        Nearby.getConnectionsClient(mainActivity).startDiscovery("com.example.gossipwars",
            endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { void ->
                Toast.makeText(mainActivity, "We are discovering", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(mainActivity, "Unable to discover", Toast.LENGTH_LONG).show()
            }
    }

    private val payloadCallback: PayloadCallback =
        object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                // This always gets the full data of the payload. Will be null if it's not a BYTES
                // payload. You can check the payload type with payload.getType().
                val receivedBytes = payload.asBytes()
                if (payload.id == MessageCode.ROOM_INFO.toLong()) {
                    var roomInfo: RoomInfo = SerializationUtils.deserialize(receivedBytes)
                    mainActivity.manageRoomInfoPayload(roomInfo, endpointId)
                }
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
                // after the call to onPayloadReceived().
            }
        }

}