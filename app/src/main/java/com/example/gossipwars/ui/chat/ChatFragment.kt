package com.example.gossipwars.ui.chat

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.entities.GameHelper
import com.example.gossipwars.logic.entities.Notifications
import com.example.gossipwars.ui.messenger.MessengerActivity


class ChatFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: AlliancesListAdapter? = null
    private var alliances: ArrayList<Alliance> = ArrayList()
    private lateinit var fragmentBarTitle: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                                savedInstanceState: Bundle?): View? {
        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        fragmentBarTitle = (context as InGameActivity).supportActionBar?.title.toString()
        subscribeToTimer()

        mRecyclerView = root.findViewById(R.id.alliances_recyclerview)
        mAdapter = AlliancesListAdapter(this, alliances)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(this.activity)

        Notifications.allianceNewStructure.observe(viewLifecycleOwner, Observer {
            alliances.clear()
            GameHelper.findAlliancesForPlayer(Game.myId)?.forEach { alliance: Alliance -> alliances.add(alliance) }
            mRecyclerView?.adapter?.notifyDataSetChanged()
        })

        val addChatFab: View = root.findViewById(R.id.add_chat_fab)
        addChatFab.setOnClickListener {
            fragmentManager?.let { AddAllianceDialogFragment().show(it, "addAllianceTag")
            }
        }

        Game.players.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it.joinToString(",") { player -> player.username },
                                Toast.LENGTH_LONG).show()
        })
        return root
    }

    fun enterAllianceChat(alliance: Alliance) {
        Toast.makeText(context, alliance.name, Toast.LENGTH_LONG).show()
        val intent = Intent(context, MessengerActivity::class.java).apply {}
        intent.putExtra("alliance", alliance.id)
        startActivity(intent)
    }

    private fun subscribeToTimer() {
        Notifications.roundTimer.observe(viewLifecycleOwner, Observer {
            if (it > 5) {
                (context as InGameActivity).supportActionBar?.title =
                    getString(R.string.bar_title, fragmentBarTitle, GameHelper.roundTimeToString(it))
            } else {
                (context as InGameActivity).supportActionBar?.title =
                    HtmlCompat.fromHtml(getString(R.string.bar_title_alert, fragmentBarTitle,
                        GameHelper.roundTimeToString(it)), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
        })
    }
}