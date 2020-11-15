package com.example.gossipwars.ui.actions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gossipwars.logic.entities.Alliance
import com.example.gossipwars.logic.entities.KickProposal
import com.example.gossipwars.logic.entities.Player
import com.example.gossipwars.logic.entities.Proposal
import java.util.*
import kotlin.collections.ArrayList

class ActionsViewModel : ViewModel() {

    private val proposalsList = ArrayList<Proposal>()

    val proposals = MutableLiveData<MutableList<Proposal>>()

    private val _text = MutableLiveData<String>().apply {
        value = "This is actions Fragment"
    }
    val text: LiveData<String> = _text

    fun addProposal() {
        var alliance = Alliance(UUID.randomUUID())
        alliance.name = "mockall"
        proposalsList.add(KickProposal(alliance, Player("mirel", UUID.randomUUID()),
                Player("marcel", UUID.randomUUID())
        ))
        proposals.value = proposalsList
    }
}