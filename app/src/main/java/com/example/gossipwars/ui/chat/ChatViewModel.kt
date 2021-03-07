package com.example.gossipwars.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.Game

class ChatViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is chat Fragment"
    }
    val text: LiveData<String> = _text
}