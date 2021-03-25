package com.example.gossipwars.logic.entities

import androidx.lifecycle.MutableLiveData

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
}