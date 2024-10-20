package com.ashomok.chatoflegends.adapters.messaging

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter
import com.ashomok.chatoflegends.R
import com.ashomok.chatoflegends.adapters.messaging.holders.HeaderHolder
import com.ashomok.chatoflegends.adapters.messaging.holders.ReceivedTextHolder
import com.ashomok.chatoflegends.adapters.messaging.holders.SentDeletedMessageHolder
import com.ashomok.chatoflegends.adapters.messaging.holders.SentTextHolder
import com.ashomok.chatoflegends.adapters.messaging.holders.TimestampHolder
import com.ashomok.chatoflegends.adapters.messaging.holders.base.BaseHolder
import com.ashomok.chatoflegends.model.realms.Message
import com.ashomok.chatoflegends.model.realms.Model
import com.ashomok.chatoflegends.utils.TimeHelper
import com.ashomok.chatoflegends.views.RealmRecyclerViewAdapter
import io.realm.OrderedRealmCollection

/**
 * Created by Devlomi on 07/08/2017.
 */
//the RealmRecyclerViewAdapter provides autoUpdate feature
//which will handle changes in list automatically with smooth animations
class MessagingAdapter(
    private val messages: OrderedRealmCollection<Message>,
    autoUpdate: Boolean,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    var model: Model,
    private val selectedItems: LiveData<List<Message>>
)
    : RealmRecyclerViewAdapter<Message, RecyclerView.ViewHolder>(
    messages,
    autoUpdate
), StickyHeaderAdapter<RecyclerView.ViewHolder> {

    private val interaction = context as? Interaction?

    //timestamps to implement the date header
    var timestamps = HashMap<Int, Long>()
    var lastTimestampPos = 0


    //date header
    override fun getHeaderId(position: Int): Long {
        return if (timestamps.containsKey(position)) {
            timestamps[position] ?: 0
        } else 0
    }

    //date header
    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_day, parent, false)
        return HeaderHolder(view)
    }

    //date header
    override fun onBindHeaderViewHolder(viewholder: RecyclerView.ViewHolder?, position: Int) {
        val mHolder = viewholder as HeaderHolder?

        //if there are no timestamps in this day then hide the header
        //otherwise show it
        val headerId = getHeaderId(position)
        if (headerId == 0L) mHolder?.header?.visibility = View.GONE else {
            val formatted = TimeHelper.getChatTime(headerId)
            mHolder?.header?.text = formatted
        }
    }

    override fun getItemCount() = messages.size


    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return message.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // check the type of view and return holder
        return getHolderByType(parent, viewType)
    }

    override fun onBindViewHolder(mHolder: RecyclerView.ViewHolder, position: Int) {

        //get itemView type
        val type = getItemViewType(position)
        val message = messages[position]



        when (type) {
            com.ashomok.chatoflegends.model.constants.MessageType.SENT_TEXT -> {
                val sentTextHolder = mHolder as SentTextHolder
                initHolder(sentTextHolder)
                sentTextHolder.bind(message, model)
            }


            com.ashomok.chatoflegends.model.constants.MessageType.RECEIVED_TEXT -> {
                val holder = mHolder as ReceivedTextHolder
                initHolder(holder)
                holder.bind(message, model)
            }

            com.ashomok.chatoflegends.model.constants.MessageType.SENT_DELETED_MESSAGE -> {
                val sentDeletedMessageHolder = mHolder as SentDeletedMessageHolder
                sentDeletedMessageHolder.bind(message, model)
            }
        }
    }

    private fun initHolder(baseHolder: BaseHolder) {
        baseHolder.selectedItems = selectedItems
        baseHolder.lifecycleOwner = lifecycleOwner
        baseHolder.interaction = interaction
    }


    private fun distinctMessagesTimestamps() {

        for (i in messages.indices) {
            val timestamp = messages[i].timestamp.toLong()
            if (i == 0) {
                timestamps[i] = timestamp
                lastTimestampPos = i
            } else {
                val oldTimestamp = messages[i - 1].timestamp.toLong()
                if (!com.ashomok.chatoflegends.utils.TimeHelper.isSameDay(timestamp, oldTimestamp)) {
                    timestamps[i] = timestamp
                    lastTimestampPos = i
                }
            }

        }

    }

    //update timestamps if needed when a new message inserted
    fun messageInserted() {
        val index = messages.size - 1
        val newTimestamp = messages[index].timestamp.toLong()
        if (timestamps.isEmpty()) {
            timestamps[index] = newTimestamp
            lastTimestampPos = index
            return
        }
        val lastTimestamp = timestamps[lastTimestampPos]!!
        if (!com.ashomok.chatoflegends.utils.TimeHelper.isSameDay(lastTimestamp, newTimestamp)) {
            timestamps[index] = newTimestamp
            lastTimestampPos = index
        }
    }


    private fun getHolderByType(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            com.ashomok.chatoflegends.model.constants.MessageType.DAY_ROW -> return TimestampHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_day, parent, false)
            )

            com.ashomok.chatoflegends.model.constants.MessageType.SENT_TEXT -> return SentTextHolder(
                context,
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_sent_message_text, parent, false)
            )

            com.ashomok.chatoflegends.model.constants.MessageType.RECEIVED_TEXT -> return ReceivedTextHolder(
                context,
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_received_message_text, parent, false)
            )

        }
        return BaseHolder(
            context,
            LayoutInflater.from(parent.context).inflate(R.layout.row_not_supported, parent, false)
        )
    }


    init {
        distinctMessagesTimestamps()
    }

}