package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.ExpressionHistoryBinding
import com.intellisoft.nndak.holders.PositioningHolder
import com.intellisoft.nndak.models.PositioningHistory

class PositioningAdapter(
    private val onItemClicked: (PositioningHistory) -> Unit
): ListAdapter<PositioningHistory, PositioningHolder>(PrescriptionItemDiffCallback()) {

    class PrescriptionItemDiffCallback : DiffUtil.ItemCallback<PositioningHistory>() {
        override fun areItemsTheSame(
            oldItem: PositioningHistory,
            newItem: PositioningHistory
        ): Boolean = oldItem.date == newItem.date

        override fun areContentsTheSame(
            oldItem: PositioningHistory,
            newItem: PositioningHistory
        ): Boolean = oldItem.date == newItem.date
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositioningHolder {
        return PositioningHolder(
            ExpressionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PositioningHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item,onItemClicked)
    }
}