package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.FeedingHistoryItemBinding
import com.intellisoft.nndak.holders.FeedingHistoryViewHolder
import com.intellisoft.nndak.models.FeedingHistory


class MonitoringAdapter() :
    ListAdapter<FeedingHistory, FeedingHistoryViewHolder>(FeedItemDiffCallback()) {

    class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedingHistory>() {
        override fun areItemsTheSame(
            oldItem: FeedingHistory,
            newItem: FeedingHistory
        ): Boolean = oldItem.date == newItem.date

        override fun areContentsTheSame(
            oldItem: FeedingHistory,
            newItem: FeedingHistory
        ): Boolean = oldItem.time == newItem.time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedingHistoryViewHolder {
        return FeedingHistoryViewHolder(
            FeedingHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder:FeedingHistoryViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item)
    }
}