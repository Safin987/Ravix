package com.terminal.key

import androidx.lifecycle.MutableLiveData

object ControlAlt {
    var isCtrl : MutableLiveData<Boolean> = MutableLiveData(false)
    var isAlt : MutableLiveData<Boolean> = MutableLiveData(false)
}