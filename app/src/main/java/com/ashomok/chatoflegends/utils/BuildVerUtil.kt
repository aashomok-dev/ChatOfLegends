package com.ashomok.chatoflegends.utils

import android.os.Build

object BuildVerUtil {

    @JvmStatic
    fun isApi29OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= 29
    }

}