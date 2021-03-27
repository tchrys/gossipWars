package com.example.gossipwars.logic.entities

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.gossipwars.communication.messages.actions.ActionEndDTO
import java.util.*


object Notifications {
    var myBonusTaken: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var allianceNewStructure: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var messageEmitter: MutableMap<Alliance, MutableLiveData<ChatMessage>> = mutableMapOf()
    var joinPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var kickPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var attackPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var defensePropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var negotiatePropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var myPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var alliancesNoForMe: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var roundTimer: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var roundOngoing: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = true }

    fun createTimeCounter() {
        roundOngoing.observeForever {
            if (it) {
                if (roundTimer.value == 0) {
                    roundTimer.value = Game.roomInfo?.roundLength
                }
            } else {
                roundTimer.value = 0
            }
        }

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object: Runnable {
            override fun run() {
                mainHandler.postDelayed(this, 1000)
                updateRoundTime()
            }
        })
    }

    private fun updateRoundTime() {
        if (roundOngoing.value == false)
            return
        if (roundTimer.value!! > 0)
            roundTimer.value = roundTimer.value?.minus(1)
        if (roundTimer.value == 0)
            Game.sendActionEnd(ActionEndDTO(Game.myId))
    }

}