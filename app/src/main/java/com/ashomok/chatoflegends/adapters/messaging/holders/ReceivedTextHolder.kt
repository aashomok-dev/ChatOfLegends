package com.ashomok.chatoflegends.adapters.messaging.holders

import android.content.Context
import android.view.View
import com.aghajari.emojiview.view.AXEmojiTextView
import com.ashomok.chatoflegends.R
import com.ashomok.chatoflegends.adapters.messaging.holders.base.BaseHolder
import com.ashomok.chatoflegends.model.realms.Message
import com.ashomok.chatoflegends.model.realms.Model

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