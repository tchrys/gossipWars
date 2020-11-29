package com.example.gossipwars.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Game

class ChatFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        chatViewModel =
            ViewModelProviders.of(this).get(ChatViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        val textView: TextView = root.findViewById(R.id.text_chat)
        chatViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        Game.players.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it.map { player -> player.username }
            .joinToString(","), Toast.LENGTH_LONG).show()
        })
        return root
    }
}