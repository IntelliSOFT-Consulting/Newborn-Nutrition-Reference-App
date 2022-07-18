package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.FeedItemViewBinding
import com.intellisoft.nndak.databinding.ItemBinding
import com.intellisoft.nndak.databinding.PrescriptionListItemBinding
import com.intellisoft.nndak.holders.FeedItemViewHolder
import com.intellisoft.nndak.models.FeedItem

class FeedAdapter (
    private val onItemClicked: (FeedItem) -> Unit
) :
    ListAdapter<FeedItem, FeedItemViewHolder>(FeedItemDiffCallback()) {

    class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(
            oldItem: FeedItem,
            newItem: FeedItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: FeedItem,
            newItem: FeedItem
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedItemViewHolder {
        return FeedItemViewHolder(
            ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: FeedItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }
}