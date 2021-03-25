package com.example.gossipwars

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.communication.components.NearbyConnectionsLogic
import com.example.gossipwars.communication.messages.MessageCode
import com.example.gossipwars.communication.messages.gameInit.RoomInfoDTO
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.apache.commons.lang3.SerializationUtils
import java.util.*


class MainActivity : AppCompatActivity() {

    var username : String? = null;
    var usernameSelected: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var acceptedUsers = mutableSetOf<String>()
    var peers = mutableSetOf<String>()
    private val roomsList = LinkedList<RoomInfoDTO>()
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RoomListAdapter? = null
    private var myRoomLength: Int = 45
    private var myRoomMaxPlayers : Int = 4
    private var gameJoined : RoomInfoDTO? = null;
    private val FINE_LOCATION_PERMISSION = 101
    private lateinit var nearbyConnectionsLogic: NearbyConnectionsLogic


    fun joinGame(roomInfo: RoomInfoDTO) {
        Log.d("DBG", roomInfo.username)
        gameJoined = roomInfo
        if (!username.equals(roomInfo.username)) {
            sendRoomPayload(roomInfo)
            Snackbar.make(findViewById(R.id.main_layout), "Wait for admin to set start",
                Snackbar.LENGTH_SHORT).show()
        } else {
            gameJoined = roomInfo
            gameJoined?.started = true
            sendRoomPayload(gameJoined!!)
            Game.roomInfo = gameJoined
            val intent = Intent(this, InGameActivity::class.java).apply {}
            startActivity(intent)
        }
    }

    fun roomEquals(myRoomInfo: RoomInfoDTO, roomToCompare : RoomInfoDTO): Boolean =
        myRoomInfo.username == roomToCompare.username && myRoomInfo.roomName == roomToCompare.roomName

    fun manageRoomInfoPayload(roomReceived: RoomInfoDTO, endpointId: String) {
        if (roomReceived.username == username) {
            var joinedRoomInfo : RoomInfoDTO = roomsList.find { roomInfo ->
                roomEquals(roomInfo, roomReceived) }!!
            if (!joinedRoomInfo.playersList.contains(endpointId)
                && joinedRoomInfo.crtPlayersNr < joinedRoomInfo.maxPlayers) {
                joinedRoomInfo.crtPlayersNr += 1
                joinedRoomInfo.playersList.add(endpointId)
                sendRoomPayload(joinedRoomInfo)
                mRecyclerView?.adapter?.notifyDataSetChanged()
                Toast.makeText(this, endpointId + " wants to join " +
                        joinedRoomInfo.roomName, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, endpointId + "updated / created " +
                    roomReceived.roomName, Toast.LENGTH_LONG).show()
            var roomInList : RoomInfoDTO? = roomsList.find { roomInfo -> roomEquals(roomInfo, roomReceived) }
            if (roomInList != null) {
                roomInList.crtPlayersNr = roomReceived.crtPlayersNr
                roomInList.playersList = roomReceived.playersList
                roomInList.started = roomReceived.started
                mRecyclerView?.adapter?.notifyDataSetChanged()
                if (roomInList.started && gameJoined?.roomName == roomInList.roomName) {
                    Game.roomInfo = roomInList
                    val intent = Intent(this, InGameActivity::class.java).apply {}
                    startActivity(intent)
                }
            } else {
                roomsList.add(roomReceived)
                mRecyclerView?.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun sendRoomPayload(roomInfo: RoomInfoDTO) {
        val data = SerializationUtils.serialize(roomInfo)
        val streamPayload = Payload.zza(data, MessageCode.ROOM_INFO.toLong())
        for (peer in peers) {
            Nearby.getConnectionsClient(this).sendPayload(peer, streamPayload)
        }
    }

    fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission)
            == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted",
                Toast.LENGTH_SHORT).show()
            nearbyConnectionsLogic.startAdvertising()
            nearbyConnectionsLogic.startDiscovery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_LONG).show()
                nearbyConnectionsLogic.startAdvertising()
                nearbyConnectionsLogic.startDiscovery()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Game.mainActivity = this
        nearbyConnectionsLogic = NearbyConnectionsLogic(this)
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION);

        // username block
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val usernameInput = findViewById<TextInputLayout>(R.id.outlinedTextField);
        val usernameText = findViewById<TextView>(R.id.usernameText);
        username = sharedPref.getString("username", "")
        if (username.orEmpty().isNotEmpty()) {
            usernameText.text = getString(R.string.username_message) + username;
            usernameSelected.value = true
        } else {
            usernameText.text = getString(R.string.please_enter_your_username)
        }
        usernameInput.placeholderText = username;

        // username button block
        val usernameButton = findViewById<Button>(R.id.username_button)
        usernameButton.setOnClickListener {
            with (sharedPref.edit()) {
                putString("username", usernameInput.editText?.text.toString())
                username = usernameInput.editText?.text.toString()
                apply()
            }
            usernameText.text = getString(R.string.username_message) + usernameInput.editText?.text.toString()
            usernameSelected.value = true
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
                myRoomLength = parent.getItemAtPosition(position).toString().dropLast(1).toInt()
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
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val myRoomInput = findViewById<TextInputLayout>(R.id.lobbyNameTextField)
        // recycler view block
        var roomListInitialized = false
        usernameSelected.observe(this, androidx.lifecycle.Observer {
            if (it && !roomListInitialized) {
                roomListInitialized = true
                val me = Player(username!!, UUID.randomUUID())
                Game.myId = me.id
                Game.players.value?.add(me)
                mRecyclerView = findViewById(R.id.recyclerview)
                mAdapter = RoomListAdapter(this, roomsList, username.orEmpty())
                mRecyclerView?.setAdapter(mAdapter)
                mRecyclerView?.setLayoutManager(LinearLayoutManager(this))
            }
        })

        // create room block
        val createRoomButton = findViewById<Button>(R.id.createRoomButton)
        createRoomButton.setOnClickListener {
            if (username.orEmpty().isEmpty()) {
                Snackbar.make(findViewById(R.id.main_layout), "No username", Snackbar.LENGTH_SHORT).show()
            }
            else if (myRoomInput.editText?.text.isNullOrEmpty()) {
                Snackbar.make(findViewById(R.id.main_layout), "No room name", Snackbar.LENGTH_SHORT).show()
            } else {
                val myRoom =
                    RoomInfoDTO(
                        username.orEmpty(), myRoomInput.editText?.text.toString(),
                        myRoomLength, myRoomMaxPlayers, 1, false
                    )
                myRoom.playersList.add(username.orEmpty())
                roomsList.addFirst(myRoom)
                gameJoined = myRoom;
                mRecyclerView?.adapter?.notifyDataSetChanged()
                sendRoomPayload(myRoom)
                myRoomInput.editText?.setText("")
            }
        }
    }

}