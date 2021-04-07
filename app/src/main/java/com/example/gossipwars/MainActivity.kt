package com.example.gossipwars

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.apache.commons.lang3.SerializationUtils
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

    private lateinit var usernameInput: TextInputLayout
    private lateinit var usernameText: TextView
    private lateinit var usernameButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var lengthSpinner: Spinner
    private lateinit var playersSpinner: Spinner
    private lateinit var myRoomInput: TextInputLayout
    private lateinit var createRoomButton: Button
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupDrawer()
        Game.mainActivity = this
        nearbyConnectionsLogic = NearbyConnectionsLogic(this)
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_PERMISSION);
        setupUsername()
        setupLengthSpinner()
        setupPlayersSpinner()
        setupRoomList()
        setupRoomCreation()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_item_how_to_play -> {
                val intent = Intent(this, HowToPlayActivity::class.java).apply {}
                startActivity(intent)
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission)
            == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            nearbyConnectionsLogic.startAdvertising()
            nearbyConnectionsLogic.startDiscovery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                nearbyConnectionsLogic.startAdvertising()
                nearbyConnectionsLogic.startDiscovery()
            }
        }
    }

    private fun setupUsername() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        usernameInput = findViewById(R.id.outlinedTextField);
        usernameText = findViewById(R.id.usernameText);
        username = sharedPref.getString("username", "")
        if (username.orEmpty().isNotEmpty()) {
            usernameText.text = getString(R.string.username_message) + username;
            usernameSelected.value = true
        } else {
            usernameText.text = getString(R.string.please_enter_your_username)
        }
        usernameInput.placeholderText = username;
        usernameButton = findViewById(R.id.username_button)
        usernameButton.setOnClickListener {
            with (sharedPref.edit()) {
                putString("username", usernameInput.editText?.text.toString())
                username = usernameInput.editText?.text.toString()
                apply()
            }
            usernameText.text = getString(R.string.username_message) + usernameInput.editText?.text.toString()
            usernameSelected.value = true
        }
    }

    private fun setupDrawer() {
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        drawer = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_closed)
        drawer.addDrawerListener(toggle)
    }

    private fun setupLengthSpinner() {
        lengthSpinner = findViewById(R.id.gameLengthSpinner)
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
    }

    private fun setupPlayersSpinner() {
        playersSpinner = findViewById(R.id.nrPlayersSpinner)
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
    }

    private fun setupRoomList() {
        myRoomInput = findViewById(R.id.lobbyNameTextField)
        var roomListInitialized = false
        usernameSelected.observe(this, androidx.lifecycle.Observer {
            if (it && !roomListInitialized) {
                roomListInitialized = true
                val me = Player(username!!, UUID.randomUUID())
                Game.myId = me.id
                Game.players.value?.add(me)
                mRecyclerView = findViewById(R.id.recyclerview)
                mAdapter = RoomListAdapter(this, roomsList, username.orEmpty())
                mRecyclerView?.adapter = mAdapter
                mRecyclerView?.layoutManager = LinearLayoutManager(this)
            }
        })
    }

    private fun setupRoomCreation() {
        createRoomButton = findViewById(R.id.createRoomButton)
        createRoomButton.setOnClickListener {
            when {
                username.orEmpty().isEmpty() -> {
                    Snackbar.make(findViewById(R.id.main_layout), "No username", Snackbar.LENGTH_SHORT).show()
                }
                myRoomInput.editText?.text.isNullOrEmpty() -> {
                    Snackbar.make(findViewById(R.id.main_layout), "No room name", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
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

    fun joinGame(roomInfo: RoomInfoDTO) {
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

    private fun roomEquals(myRoomInfo: RoomInfoDTO, roomToCompare : RoomInfoDTO): Boolean =
        myRoomInfo.username == roomToCompare.username && myRoomInfo.roomName == roomToCompare.roomName

    fun manageRoomInfoPayload(roomReceived: RoomInfoDTO, endpointId: String) {
        if (roomReceived.username == username) {
            val joinedRoomInfo : RoomInfoDTO = roomsList.find { roomInfo ->
                roomEquals(roomInfo, roomReceived) }!!
            if (!joinedRoomInfo.playersList.contains(endpointId)
                && joinedRoomInfo.crtPlayersNr < joinedRoomInfo.maxPlayers) {
                joinedRoomInfo.crtPlayersNr += 1
                joinedRoomInfo.playersList.add(endpointId)
                sendRoomPayload(joinedRoomInfo)
                mRecyclerView?.adapter?.notifyDataSetChanged()
            }
        } else {
            val roomInList : RoomInfoDTO? = roomsList.find { roomInfo -> roomEquals(roomInfo, roomReceived) }
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



}