package com.example.gossipwars.ui.messenger

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.actions.MembersActionDTO
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.ChatMessage
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import kotlin.collections.ArrayList

class MessengerActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: MessagesListAdapter? = null
    private lateinit var activityAllianceId: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)
        setSupportActionBar(findViewById(R.id.chatToolbar))

        val allianceId: UUID = (intent.getSerializableExtra("alliance") as UUID)
        val alliance: Alliance = Game.findAllianceByUUID(allianceId)
        activityAllianceId = allianceId
        Toast.makeText(this, alliance.name, Toast.LENGTH_LONG).show()

        mRecyclerView = findViewById(R.id.chatMessagesRecyclerView)
        mAdapter = MessagesListAdapter(this, alliance.messageList, Game.findPlayerByUUID(Game.myId).username)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(this)

        Game.messageEmitter[alliance]?.observe(this, Observer {
            mRecyclerView?.adapter?.notifyDataSetChanged()
//            mRecyclerView?.adapter?.notifyItemInserted(alliance.messageList.size)
//            mRecyclerView?.scrollToPosition(alliance.messageList.size)
        })



        supportActionBar?.title = alliance.name
        supportActionBar?.subtitle = alliance.playersInvolved.map { player -> player.username }
            .joinToString(",")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val chatInput: TextInputEditText? = findViewById(R.id.chatInputText)
        chatInput?.inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        val sendButton = findViewById<Button>(R.id.chatSendButton)
        sendButton.setOnClickListener { view ->
            Game.sendMessage(ChatMessage(alliance, chatInput?.text.toString(),
                                    Game.findPlayerByUUID(Game.myId)).convertToDTO())
            chatInput?.setText("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messenger_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.actionQuitAlliance) {
            Toast.makeText(this, "Quit alliance", Toast.LENGTH_LONG).show()
            val meAsAPlayer: Player = Game.findPlayerByUUID(Game.myId)
            Game.sendMembersAction(MembersActionDTO(Game.myId, Game.myId, activityAllianceId,
                                    ProposalEnum.KICK))
            onBackPressed()
            return true
        } else {
            onBackPressed()
        }
        return true
    }
}