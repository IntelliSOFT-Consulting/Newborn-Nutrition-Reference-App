package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.databinding.PrescriptionListItemBinding
import com.intellisoft.nndak.holders.PrescriptionItemViewHolder
import com.intellisoft.nndak.models.PrescriptionItem

class PrescriptionAdapter(
    private val onItemClicked: (PrescriptionItem) -> Unit
) :
    ListAdapter<PrescriptionItem, PrescriptionItemViewHolder>(PrescriptionItemDiffCallback()) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionItemViewHolder {
        return PrescriptionItemViewHolder(
            PrescriptionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PrescriptionItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }
}