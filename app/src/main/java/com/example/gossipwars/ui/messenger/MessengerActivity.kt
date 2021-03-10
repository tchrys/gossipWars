package com.example.gossipwars.ui.messenger

import android.os.Bundle
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
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.ChatMessage
import com.example.gossipwars.logic.entities.Game
import com.google.android.material.textfield.TextInputEditText

class MessengerActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: MessagesListAdapter? = null
    private var messages: ArrayList<ChatMessage> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)
        setSupportActionBar(findViewById(R.id.chatToolbar))

        var alliance: Alliance = (intent.getSerializableExtra("alliance") as Alliance)
        Toast.makeText(this, alliance.name, Toast.LENGTH_LONG).show()

        alliance.messageList.forEach { chatMessage: ChatMessage -> messages.add(chatMessage) }

        mRecyclerView = findViewById(R.id.chatMessagesRecyclerView)
        mAdapter = MessagesListAdapter(this, messages)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(this)

        Game.messageEmitter[Game.findAllianceByUUID(alliance.id)]?.observe(this, Observer {
            Log.d("DBG", it.content)
            messages.add(ChatMessage(it.alliance, it.content, it.sender))
            Log.d("DBG", messages.size.toString())
            Log.d("DBG", Game.findAllianceByUUID(alliance.id).messageList.size.toString())
            mRecyclerView?.adapter?.notifyDataSetChanged()
        })

        supportActionBar?.title = alliance.name
        supportActionBar?.subtitle = alliance.playersInvolved.map { player -> player.username }
            .joinToString(",")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val chatInput: TextInputEditText? = findViewById(R.id.chatInputText)
        val sendButton = findViewById<Button>(R.id.chatSendButton)
        sendButton.setOnClickListener { view ->
            Game.sendMessage(ChatMessage(alliance, chatInput?.text.toString(),
                                    Game.findPlayerByUUID(Game.myId)).convertToDTO()) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messenger_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.actionQuitAlliance) {
            Toast.makeText(this, "Quit alliance", Toast.LENGTH_LONG).show()
            return true
        } else {
            onBackPressed()
        }
        return true
    }
}