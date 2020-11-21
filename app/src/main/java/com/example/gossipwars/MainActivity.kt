package com.example.gossipwars

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.logic.entities.RoomInfo
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.apache.commons.lang3.SerializationUtils
import java.util.*


class MainActivity : AppCompatActivity() {

    private val roomsList = LinkedList<RoomInfo>()
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RoomListAdapter? = null
    private var myRoomLength: Int = 45
    private var myRoomMaxPlayers : Int = 4
    private var gameJoined : RoomInfo? = null;
    private var username : String? = null;
    private var acceptedUsers = mutableSetOf<String>()
    private var peers = mutableSetOf<String>()

    private val payloadCallback: PayloadCallback =
        object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                // This always gets the full data of the payload. Will be null if it's not a BYTES
                // payload. You can check the payload type with payload.getType().
                val receivedBytes = payload.asBytes()
                var roomReceived : RoomInfo = SerializationUtils.deserialize(receivedBytes)
                if (roomReceived.username.equals(username)) {
                    var joinedRoomInfo : RoomInfo = roomsList.find { roomInfo ->
                        roomInfo.username.equals(username) }!!
                    if (!joinedRoomInfo.playersList.contains(endpointId)
                        && joinedRoomInfo.crtPlayersNr < joinedRoomInfo.maxPlayers) {
                        joinedRoomInfo.crtPlayersNr += 1
                        joinedRoomInfo.playersList.add(endpointId)
                        Toast.makeText(applicationContext, endpointId + " wants to join " + joinedRoomInfo.roomName, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(applicationContext, endpointId + "updated / created " + roomReceived.roomName, Toast.LENGTH_LONG).show()
                }
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
                // after the call to onPayloadReceived().
            }
        }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // an endpoint was found. we request a connection to it
                Nearby.getConnectionsClient(applicationContext)
                    .requestConnection(username.orEmpty(), endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener { void ->
                        // we successfully requested a connection. now both sides
                        // must accept before the connection is established
                        Toast.makeText(applicationContext, "Request connection", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { exception ->
                        // nearby connections failed to request the connection
                        Toast.makeText(applicationContext, "Request failed", Toast.LENGTH_LONG).show()
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
                        if (acceptedUsers.contains(endpointId)) {
                            peers.add(endpointId)
                        }
                        Toast.makeText(applicationContext, "Status ok", Toast.LENGTH_LONG).show()
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
               AlertDialog.Builder(applicationContext)
                   .setTitle("Accept connection to " + connectionInfo.endpointName)
                   .setMessage("Confirm the code matches on both devices " + connectionInfo.authenticationToken)
                   .setPositiveButton("Accept") {
                       dialogInterface, i ->
                       // the user confirmed, so we can accept the connection
                       Nearby.getConnectionsClient(applicationContext)
                           .acceptConnection(endpointId, payloadCallback)
                       acceptedUsers.add(endpointId)
                       Toast.makeText(applicationContext, endpointId, Toast.LENGTH_LONG).show()
                   }
                   .setNegativeButton("Cancel") {
                       dialogInterface, i ->
                       // the user canceled, so we should reject the connection
                       Nearby.getConnectionsClient(applicationContext).rejectConnection(endpointId)
                   }
                   .setIcon(android.R.drawable.ic_dialog_alert)
                   .show()
                }
            }

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising(username.orEmpty(), "com.example.gossipwars",
                connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener { void ->
                Toast.makeText(applicationContext, "We are advertising", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Unable to advertise", Toast.LENGTH_LONG).show()
            }
    }

    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        Nearby.getConnectionsClient(applicationContext).startDiscovery("com.example.gossipwars",
            endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener { void ->
                Toast.makeText(applicationContext, "We are discovering", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Unable to discover", Toast.LENGTH_LONG).show()
            }
    }

    private fun sendRoomPayload(roomInfo: RoomInfo) {
        val data = SerializationUtils.serialize(roomInfo)
        val streamPayload = Payload.fromBytes(data)
        for (peer in peers) {
            Nearby.getConnectionsClient(applicationContext).sendPayload(peer, streamPayload)
        }
    }

    fun joinGame(roomInfo: RoomInfo) {
        Log.d("DBG", roomInfo.username)
        gameJoined = roomInfo
        if (!username.equals(roomInfo.username)) {
            sendRoomPayload(roomInfo)

            Snackbar.make(findViewById(R.id.main_layout), "Wait for admin to set start",
                Snackbar.LENGTH_SHORT).show()
        } else {
            // TODO a lot
            val intent = Intent(this, InGameActivity::class.java).apply {
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // username block
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val usernameInput = findViewById<TextInputLayout>(R.id.outlinedTextField);
        val usernameText = findViewById<TextView>(R.id.usernameText);
        username = sharedPref.getString("username", "")
        if (username.orEmpty().isNotEmpty()) {
            usernameText.text = getString(R.string.username_message) + username;
        } else {
            usernameText.text = "Please enter your username"
        }
        usernameInput.placeholderText = username;

        Toast.makeText(applicationContext, "Before starting nearby connections", Toast.LENGTH_LONG).show()
        startAdvertising()
        startDiscovery()


        // username button block
        val usernameButton = findViewById<Button>(R.id.username_button)
        usernameButton.setOnClickListener {
            with (sharedPref.edit()) {
                putString("username", usernameInput.editText?.text.toString())
                username = usernameInput.editText?.text.toString()
                apply()
            }
            usernameText.text = getString(R.string.username_message) + usernameInput.editText?.text.toString()
        }

        // length spinner block
        val lengthSpinner : Spinner = findViewById(R.id.gameLengthSpinner)
        ArrayAdapter.createFromResource(this, R.array.game_length_array,
            android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lengthSpinner.adapter = adapter
        }
        lengthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                myRoomLength = parent.getItemAtPosition(position).toString().dropLast(1).toInt();
//                Log.d("DBG", parent.getItemAtPosition(position).toString().dropLast(1))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // players spinner block
        val playersSpinner : Spinner = findViewById(R.id.nrPlayersSpinner)
        ArrayAdapter.createFromResource(this, R.array.nr_players_array,
            android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            playersSpinner.adapter = adapter
        }
        playersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                myRoomMaxPlayers = parent.getItemAtPosition(position).toString().toInt();
//                Log.d("DBG", parent.getItemAtPosition(position).toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        var myRoomInput = findViewById<TextInputLayout>(R.id.lobbyNameTextField)
        // recycler view block
        if (!username.isNullOrEmpty()) {
            mRecyclerView = findViewById(R.id.recyclerview)
            mAdapter = RoomListAdapter(this, roomsList, username.orEmpty())
            mRecyclerView?.setAdapter(mAdapter)
            mRecyclerView?.setLayoutManager(LinearLayoutManager(this))
        }

        // create room block
        val createRoomButton = findViewById<Button>(R.id.createRoomButton)
        createRoomButton.setOnClickListener {
            if (username.orEmpty().isEmpty()) {
                Snackbar.make(findViewById(R.id.main_layout), "No username", Snackbar.LENGTH_SHORT).show()
            }
            else if (myRoomInput.editText?.text.isNullOrEmpty()) {
                Snackbar.make(findViewById(R.id.main_layout), "No room name", Snackbar.LENGTH_SHORT).show()
            } else {
                var myRoom = RoomInfo(username.orEmpty(),
                    myRoomInput.editText?.text.toString(), myRoomLength, myRoomMaxPlayers)
                myRoom.playersList.add(username.orEmpty())
                roomsList.addFirst(myRoom)
                gameJoined = myRoom;
                mRecyclerView?.adapter?.notifyDataSetChanged()
                sendRoomPayload(myRoom)
            }
        }



    }

}