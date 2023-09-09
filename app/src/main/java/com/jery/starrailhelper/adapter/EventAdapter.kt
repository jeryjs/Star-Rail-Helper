package com.jery.starrailhelper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.jery.starrailhelper.R
import com.jery.starrailhelper.data.EventItem
import com.jery.starrailhelper.databinding.ItemEventBinding
import com.jery.starrailhelper.utils.Utils

class EventAdapter (
    private val events: MutableList<EventItem>
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val eventItem = getItem(position)
        holder.bind(eventItem)
    }

    override fun getItemCount(): Int {
        return if (events.size>10) 10 else events.size
    }

    private fun getItem(position: Int): EventItem {
        return events[position]
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(eventItem: EventItem) {
            val ctx = itemView.context
            with (binding) {
                this.eventItem = eventItem
                this.utils = Utils
                executePendingBindings()
                itemView.setOnLongClickListener { eventItem.isSeen = !eventItem.isSeen; notifyItemChanged(layoutPosition); false }
            }
            try {
                Glide.with(ctx)
                    .load(eventItem.image)
                    .placeholder(R.drawable.ic_timer)
                    .fitCenter()
                    .transition(
                        DrawableTransitionOptions.withCrossFade(
                            DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
                        )
                    )
                    .into(binding.ivBanner)
            } catch (e: Exception) {
                Utils.showStackTrace(ctx, e)
                e.printStackTrace()
            }
        }
    }
}