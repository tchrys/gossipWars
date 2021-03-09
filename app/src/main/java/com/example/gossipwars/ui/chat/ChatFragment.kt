package com.example.gossipwars.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.MainActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game
import com.example.gossipwars.logic.proposals.Proposal
import com.example.gossipwars.ui.actions.ProposalListAdapter
import com.example.gossipwars.ui.messenger.MessengerActivity
import com.google.android.material.snackbar.Snackbar

class ChatFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: AlliancesListAdapter? = null
    private var alliances: ArrayList<Alliance> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                                savedInstanceState: Bundle?): View? {
        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_chat, container, false)

        mRecyclerView = root.findViewById(R.id.alliances_recyclerview)
        mAdapter = AlliancesListAdapter(this, alliances)
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(this.activity)

        Game.allianceNewStructure.observe(viewLifecycleOwner, Observer {
            alliances.clear()
            Game.alliances.forEach { alliance: Alliance -> alliances.add(alliance) }
            mRecyclerView?.adapter?.notifyDataSetChanged()
        })

        val addChatFab: View = root.findViewById(R.id.add_chat_fab)
        addChatFab.setOnClickListener { view ->
            fragmentManager?.let { AddAllianceDialogFragment().show(it, "addAllianceTag")
            }
        }

        Game.players.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it.map { player -> player.username }
            .joinToString(","), Toast.LENGTH_LONG).show()
        })
        return root
    }

    fun enterAllianceChat(alliance: Alliance) {
        Toast.makeText(context, alliance.name, Toast.LENGTH_LONG).show()
        val intent = Intent(context, MessengerActivity::class.java).apply {}
        intent.putExtra("alliance", alliance)
        startActivity(intent)
    }
}