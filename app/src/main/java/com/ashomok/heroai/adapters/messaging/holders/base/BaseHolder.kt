package com.ashomok.heroai.adapters.messaging.holders.base

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.ashomok.heroai.R
import com.ashomok.heroai.adapters.messaging.Interaction
import com.ashomok.heroai.model.realms.Model


open class BaseHolder(val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvTime: TextView? = itemView.findViewById(R.id.tv_time)
    var container: View? = itemView.findViewById(R.id.container)


    var interaction: Interaction? = null

    //to keep track of selected Items
    var lifecycleOwner: LifecycleOwner? = null

    var selectedItems: LiveData<List<com.ashomok.heroai.model.realms.Message>>? = null
    open fun bind(
        message: com.ashomok.heroai.model.realms.Message,
        model: Model
    ) {
        tvTime?.text = message.time

        val progressIdleIconRes =
            if (com.ashomok.heroai.model.constants.MessageType.isSentType(message.type)) R.drawable.ic_file_upload else R.drawable.ic_file_download
        val progressIdleIcon = AppCompatResources.getDrawable(context, progressIdleIconRes)!!
        progressIdleIcon.tint(context, R.color.white)


        itemView.setOnClickListener {
            interaction?.onItemViewClick(adapterPosition, itemView, message)
        }

        container?.setOnClickListener {
            interaction?.onContainerViewClick(adapterPosition, itemView, message)
        }


        itemView.setOnLongClickListener {
            interaction?.onLongClick(adapterPosition, itemView, message)
            true
        }

        container?.setOnLongClickListener {
            interaction?.onLongClick(adapterPosition, itemView, message)
            true
        }

        lifecycleOwner?.let {
            selectedItems?.observe(it) { selectedMessages ->
                val contains = selectedMessages.contains(message)
                setBackgroundColor(itemView, contains)
            }
        }
    }

    fun Drawable.tint(context: Context, @ColorRes color: Int) {
        DrawableCompat.setTintMode(this, PorterDuff.Mode.SRC_IN)
        DrawableCompat.setTint(this, ContextCompat.getColor(context, color))
    }


    //set background color of item if it's selected
    private fun setBackgroundColor(view: View, isAdded: Boolean) {
        val addedColor = context.resources.getColor(R.color.item_selected_background_color)
        val notAddedColor = 0x00000000
        if (isAdded) view.setBackgroundColor(addedColor) else view.setBackgroundColor(notAddedColor)
    }

}


