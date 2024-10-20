package com.ashomok.chatoflegends.activities.main.messaging

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.ashomok.chatoflegends.R
import com.ashomok.chatoflegends.model.realms.Chat
import com.ashomok.chatoflegends.utils.LogHelper
import com.ashomok.chatoflegends.utils.SharedPreferencesManager
import com.ashomok.chatoflegends.utils.TimeHelper
import java.lang.Long.parseLong

class ChatListAdapter(private val context: Context, resource: Int) : ArrayAdapter<Chat>(
    context, resource
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View =
            convertView ?: LayoutInflater.from(context).inflate(
                R.layout.row_chat, parent,
                false
            )
        try {
            val item: Chat? = getItem(position)
            val chatIcon: ImageView = view.findViewById(R.id.chat_icon)
            val chatTitle: TextView = view.findViewById(R.id.chat_title)
            val lastMessagePreview: TextView = view.findViewById(R.id.last_message_preview)
            val lastMessageTime: TextView = view.findViewById(R.id.last_message_time)
            val chatPrice: TextView = view.findViewById(R.id.chat_price)
            chatPrice.text =
                String.format(context.getString(R.string.chat_price), item?.model?.tokenNeeds)
            if (item?.model?.tokenNeeds!! >= 30) {
                chatPrice.backgroundTintList = context.getColorStateList(R.color.red_500)
            } else if (item.model?.tokenNeeds!! >= 10) {
                chatPrice.backgroundTintList =
                    context.getColorStateList(R.color.orange_500)
            } else if (item.model?.tokenNeeds!! > 0) {
                chatPrice.backgroundTintList =
                    context.getColorStateList(R.color.green_500)
            }
            chatPrice.setOnClickListener {
                AlertDialog.Builder(view.context)
                    .setTitle(
                        String.format(
                            context.getString(R.string.one_message_costs),
                            item.model?.tokenNeeds
                        )
                    )
                    .setPositiveButton(
                        view.resources.getString(R.string.ok)
                    ) { _: DialogInterface?, _: Int -> }
                    .show()
            }

            val bestChoice: TextView = view.findViewById(R.id.best_choice)
            if (item.model?.isBestChoice == true) {
                bestChoice.visibility = VISIBLE
                bestChoice.setOnClickListener {
                    AlertDialog.Builder(view.context)
                        .setTitle(view.resources.getString(R.string.we_recommend_this_option_explanation))
                        .setPositiveButton(
                            view.resources.getString(R.string.ok)
                        ) { _: DialogInterface?, _: Int -> }
                        .show()
                }
            } else {
                bestChoice.visibility = GONE
            }

            val about: TextView = view.findViewById(R.id.about)
            var aboutStringResId = 0
            var iconDrawId = 0;
            if (item.model?.equals(SharedPreferencesManager.getHomelessModel()) == true) {
                aboutStringResId = R.string.about_homeless
                iconDrawId = R.drawable.homeless
            } else if (item.model?.equals(SharedPreferencesManager.getSocraticModel()) == true) {
                aboutStringResId = R.string.about_socratic
                iconDrawId = R.drawable.socratic
            } else if (item.model?.equals(SharedPreferencesManager.getEinsteinModel()) == true) {
                aboutStringResId = R.string.about_einstein
                iconDrawId = R.drawable.einstein
            } else if (item.model?.equals(SharedPreferencesManager.getTeslaModel()) == true) {
                aboutStringResId = R.string.about_tesla
                iconDrawId = R.drawable.tesla
            } else if (item.model?.equals(SharedPreferencesManager.getGypsyWomanModel()) == true) {
                aboutStringResId = R.string.about_gypsy_woman
                iconDrawId = R.drawable.gypsy_woman
            } else if (item.model?.equals(SharedPreferencesManager.getElonMuskModel()) == true) {
                aboutStringResId = R.string.about_elon_musk
                iconDrawId = R.drawable.elon_musk
            }
            chatIcon.setImageResource(iconDrawId)
            about.setOnClickListener {
                AlertDialog.Builder(view.context)
                    .setTitle(item.model?.modelNamePretty!!)
                    .setMessage(view.resources.getString(aboutStringResId))
                    .setPositiveButton(
                        view.resources.getString(R.string.ok)
                    ) { _: DialogInterface?, _: Int -> }
                    .show()
            }

            chatTitle.text = item.model?.modelNamePretty ?: ""
            lastMessagePreview.text = item.lastMessage?.content ?: ""
            val formatted = item.lastMessageTimestamp?.let { parseLong(it) }?.let {
                TimeHelper.getChatTimeShorted(
                    context,
                    it
                )
            }
            lastMessageTime.text = formatted
        } catch (e: Exception) {
            LogHelper.e(TAG, e.message)
        }
        return view
    }

    companion object {
        private val TAG: String = ChatListAdapter::class.java.getSimpleName()
    }
}

