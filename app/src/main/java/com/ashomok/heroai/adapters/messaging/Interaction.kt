package com.ashomok.heroai.adapters.messaging

import android.view.View

interface Interaction {
    fun onContainerViewClick(
        pos: Int,
        itemView: View,
        message: com.ashomok.heroai.model.realms.Message
    )

    fun onItemViewClick(pos: Int, itemView: View, message: com.ashomok.heroai.model.realms.Message)
    fun onLongClick(pos: Int, itemView: View, message: com.ashomok.heroai.model.realms.Message)
    fun onProgressButtonClick(
        pos: Int,
        itemView: View,
        message: com.ashomok.heroai.model.realms.Message
    )

}