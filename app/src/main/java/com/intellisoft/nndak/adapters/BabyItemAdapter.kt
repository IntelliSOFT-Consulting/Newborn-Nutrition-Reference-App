package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.holders.BabyItemViewHolder
import com.intellisoft.nndak.models.MotherBabyItem

class BabyItemAdapter(
    private val onItemClicked: (MotherBabyItem) -> Unit
) :
    ListAdapter<MotherBabyItem, BabyItemViewHolder>(MotherBabyItemDiffCallback()) {

    class MotherBabyItemDiffCallback : DiffUtil.ItemCallback<MotherBabyItem>() {
        override fun areItemsTheSame(
            oldItem: MotherBabyItem,
            newItem: MotherBabyItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: MotherBabyItem,
            newItem: MotherBabyItem
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BabyItemViewHolder {
        return BabyItemViewHolder(
            BabyListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
    override fun onBindViewHolder(holder: BabyItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }
}