package com.example.gossipwars

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.logic.entities.RoomInfo
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.util.*


class MainActivity : AppCompatActivity() {

    private val roomsList = LinkedList<RoomInfo>()
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RoomListAdapter? = null
    private var myRoomLength: Int = 45
    private var myRoomMaxPlayers : Int = 4
    private var gameJoined : RoomInfo? = null;
    private var username : String? = null;

    fun joinGame(roomInfo: RoomInfo) {
        Log.d("DBG", roomInfo.username)
        gameJoined = roomInfo
        if (!username.equals(roomInfo.username)) {
            Snackbar.make(findViewById(R.id.main_layout), "Wait for admin to set start", Snackbar.LENGTH_SHORT).show()
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
                Log.d("DBG", parent.getItemAtPosition(position).toString().dropLast(1))
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
                Log.d("DBG", parent.getItemAtPosition(position).toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        var myRoomInput = findViewById<TextInputLayout>(R.id.lobbyNameTextField)
        // recycler view block
        for (i in 0..5) {
            roomsList.addLast(RoomInfo("marcel$i", "chitila",
                45, 7, 1));
        }
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
            }
        }



    }

}