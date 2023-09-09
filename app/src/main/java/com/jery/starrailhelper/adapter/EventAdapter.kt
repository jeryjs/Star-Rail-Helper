package com.jery.starrailhelper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            with (binding) {
                this.eventItem = eventItem
                this.utils = Utils

                executePendingBindings()
            }
        }
    }
}