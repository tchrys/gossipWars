package com.example.gossipwars.ui.messenger

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.communication.messages.actions.MembersActionDTO
import com.example.gossipwars.logic.entities.*
import com.example.gossipwars.logic.proposals.ProposalEnum
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import kotlin.collections.ArrayList

class MessengerActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: MessagesListAdapter? = null
    private lateinit var activityAllianceId: UUID
    private lateinit var alliance: Alliance
    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)
        setSupportActionBar(findViewById(R.id.chatToolbar))
        frameLayout = findViewById(R.id.progress_view)

        val allianceId: UUID = (intent.getSerializableExtra("alliance") as UUID)
        alliance = GameHelper.findAllianceByUUID(allianceId)
        activityAllianceId = allianceId

        subscribeToTimer()
        supportActionBar?.subtitle = alliance.playersInvolved.map { player -> player.username }
            .joinToString(",")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        frameLayout.visibility = View.VISIBLE

        Notifications.roundOngoing.observe(this, Observer {
            frameLayout.visibility = if (it) View.GONE else View.VISIBLE
        })


        mRecyclerView = findViewById(R.id.chatMessagesRecyclerView)
        mAdapter = MessagesListAdapter(
            this,
            alliance.messageList,
            GameHelper.findPlayerByUUID(Game.myId).username
        )
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(this)

        Notifications.messageEmitter[alliance]?.observe(this, Observer {
            mRecyclerView?.adapter?.notifyDataSetChanged()
//            mRecyclerView?.adapter?.notifyItemInserted(alliance.messageList.size)
//            mRecyclerView?.scrollToPosition(alliance.messageList.size)
        })

        val chatInput: TextInputEditText? = findViewById(R.id.chatInputText)
        chatInput?.inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        val sendButton = findViewById<Button>(R.id.chatSendButton)
        sendButton.setOnClickListener { view ->
            Game.sendMessage(
                ChatMessage(
                    alliance, chatInput?.text.toString(),
                    GameHelper.findPlayerByUUID(Game.myId)
                ).convertToDTO()
            )
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
            Game.sendMembersAction(
                MembersActionDTO(
                    Game.myId, Game.myId, activityAllianceId,
                    ProposalEnum.KICK
                )
            )
            onBackPressed()
            return true
        } else {
            onBackPressed()
        }
        return true
    }

    private fun subscribeToTimer() {
        Notifications.roundTimer.observe(this, Observer {
            if (it > 5) {
                supportActionBar?.title =
                    getString(R.string.bar_title, alliance.name, GameHelper.roundTimeToString(it))
            } else {
                supportActionBar?.title =
                    HtmlCompat.fromHtml(getString(R.string.bar_title_alert, alliance.name,
                        GameHelper.roundTimeToString(it)), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        })
    }
}