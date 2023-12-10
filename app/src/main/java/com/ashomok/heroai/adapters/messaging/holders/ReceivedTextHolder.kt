package com.ashomok.heroai.adapters.messaging.holders

import android.content.Context
import android.view.View
import com.aghajari.emojiview.view.AXEmojiTextView
import com.ashomok.heroai.R
import com.ashomok.heroai.adapters.messaging.holders.base.BaseHolder
import com.ashomok.heroai.model.realms.Message
import com.ashomok.heroai.model.realms.Model

// received message with type text
class ReceivedTextHolder(context: Context, itemView: View) : BaseHolder(context, itemView) {

    private var tvMessageContent: AXEmojiTextView = itemView.findViewById(R.id.tv_message_content)

    override fun bind(message: Message,
                      model: Model
    ) {
        super.bind(message, model)
        tvMessageContent.text = message.content
    }


}