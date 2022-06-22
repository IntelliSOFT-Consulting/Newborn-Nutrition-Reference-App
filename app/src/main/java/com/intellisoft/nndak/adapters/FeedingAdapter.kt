package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.FeedingItemBinding
import com.intellisoft.nndak.holders.FeedingHolder
import com.intellisoft.nndak.models.PrescriptionItem

class FeedingAdapter() :
    ListAdapter<PrescriptionItem, FeedingHolder>(PrescriptionItemDiffCallback()) {

    class PrescriptionItemDiffCallback : DiffUtil.ItemCallback<PrescriptionItem>() {
        override fun areItemsTheSame(
            oldItem: PrescriptionItem,
            newItem: PrescriptionItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: PrescriptionItem,
            newItem: PrescriptionItem
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedingHolder {
        return FeedingHolder(
            FeedingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: FeedingHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item)
    }
}