package com.ashomok.heroai.adapters.messaging.holders.base

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.ashomok.heroai.R
import com.ashomok.heroai.model.realms.Message
import com.ashomok.heroai.model.realms.Model
import com.ashomok.heroai.utils.AdapterHelper

open class BaseSentHolder(context: Context, itemView: View) : BaseHolder(context, itemView) {

    var messageStatImg: ImageView? = itemView.findViewById(R.id.message_stat_img)

    override fun bind(message: Message, model: Model) {
        super.bind(message, model)
        //imgStat (received or read)
        messageStatImg?.setImageResource(AdapterHelper.getMessageStatDrawable(message.messageStat))
    }
}

