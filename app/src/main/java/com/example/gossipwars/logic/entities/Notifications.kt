package com.example.gossipwars.logic.entities

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.gossipwars.communication.messages.actions.ActionEndDTO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


object Notifications {
    var myBonusTaken: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var allianceNewStructure: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var messageEmitter: MutableMap<UUID, MutableLiveData<ChatMessage>> = mutableMapOf()
    var joinPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var kickPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var attackPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var defensePropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var negotiatePropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var myPropsNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var alliancesNoForMe: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    var roundTimer: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 2 }
    var roundOngoing: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    var crtRoundNo: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = -1 }
    var roundStoppedFor: MutableLiveData<Int> = MutableLiveData<Int>().apply { value = 0 }
    lateinit var timeCounterHandler: Handler

    val roundOngoingObserver = Observer<Boolean> {
        if (Game.gameStarted) {
            if (it) {
                if (roundTimer.value == 0) {
                    roundTimer.value = Game.roomInfo?.roundLength
                }
            } else {
                roundTimer.value = 0
            }
        }
    }

    object CounterRunnable: Runnable {
        override fun run() {
            timeCounterHandler.postDelayed(this, 1000)
            updateRoundTime()
        }
    }

    fun createTimeCounter() {
        roundOngoing.observeForever(roundOngoingObserver)

        timeCounterHandler = Handler(Looper.getMainLooper())
        timeCounterHandler.post(CounterRunnable)
    }

    fun cleanup() {
        roundOngoing.removeObserver(roundOngoingObserver)
        timeCounterHandler.removeCallbacks(CounterRunnable)

        myBonusTaken.value = false
        allianceNewStructure.value = false
        messageEmitter.clear()
        joinPropsNo.value = 0
        kickPropsNo.value = 0
        attackPropsNo.value = 0
        defensePropsNo.value = 0
        negotiatePropsNo.value = 0
        myPropsNo.value = 0
        alliancesNoForMe.value = 0
        roundTimer.value = 2
//        roundOngoing.value = false
        crtRoundNo.value = -1
        roundStoppedFor.value = 0
    }

    private fun updateRoundTime() {
        if (!Game.gameStarted)
            return
        if (roundOngoing.value == false) {
            Log.d("DBG", roundStoppedFor.value.toString())
            roundStoppedFor.value = roundStoppedFor.value?.plus(1)
            return
        }
        roundStoppedFor.value = 0
        if (roundTimer.value!! > 0)
            roundTimer.value = roundTimer.value?.minus(1)
        if (roundTimer.value == 0)
            GlobalScope.launch { Game.sendActionEnd(ActionEndDTO(Game.myId)) }
    }

}