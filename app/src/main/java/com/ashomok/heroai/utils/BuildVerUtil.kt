package com.ashomok.heroai.utils

import android.os.Build

object BuildVerUtil {

    @JvmStatic
    fun isApi29OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= 29
    }

}