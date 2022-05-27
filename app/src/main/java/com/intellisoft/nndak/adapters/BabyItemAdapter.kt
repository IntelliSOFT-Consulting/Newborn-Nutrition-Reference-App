package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.holders.BabyItemViewHolder
import com.intellisoft.nndak.models.PatientItem

class BabyItemAdapter(
    private val onItemClicked: (PatientItem) -> Unit
) :
    ListAdapter<PatientItem, BabyItemViewHolder>(PatientItemDiffCallback()) {

    class PatientItemDiffCallback : DiffUtil.ItemCallback<PatientItem>() {
        override fun areItemsTheSame(
            oldItem: PatientItem,
            newItem: PatientItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: PatientItem,
            newItem: PatientItem
        ): Boolean = oldItem.id == newItem.id && oldItem.risk == newItem.risk
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