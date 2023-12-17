package com.ashomok.chatoflegends.adapters.messaging.holders

import android.content.Context
import android.view.View
import com.aghajari.emojiview.view.AXEmojiTextView
import com.ashomok.chatoflegends.R
import com.ashomok.chatoflegends.adapters.messaging.holders.base.BaseSentHolder
import com.ashomok.chatoflegends.model.realms.Model


// sent message with type text
class SentTextHolder(context: Context, itemView: View) : BaseSentHolder(context, itemView) {
    private var tvMessageContent: AXEmojiTextView = itemView.findViewById(R.id.tv_message_content)

    override fun bind(
        message: com.ashomok.chatoflegends.model.realms.Message,
        model: Model
    ) {
        super.bind(message, model)
        tvMessageContent.text = message.content
    }

}

