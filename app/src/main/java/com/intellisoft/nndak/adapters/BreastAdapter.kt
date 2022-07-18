package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.ExpressionHistoryBinding
import com.intellisoft.nndak.holders.BreastHolder
import com.intellisoft.nndak.holders.PositioningHolder
import com.intellisoft.nndak.models.BreastsHistory


class BreastAdapter(
    private val onItemClicked: (BreastsHistory) -> Unit
) : ListAdapter<BreastsHistory, BreastHolder>(PrescriptionItemDiffCallback()) {

    class PrescriptionItemDiffCallback : DiffUtil.ItemCallback<BreastsHistory>() {
        override fun areItemsTheSame(
            oldItem: BreastsHistory,
            newItem: BreastsHistory
        ): Boolean = oldItem.date == newItem.date

        override fun areContentsTheSame(
            oldItem: BreastsHistory,
            newItem: BreastsHistory
        ): Boolean = oldItem.date == newItem.date
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreastHolder {
        return BreastHolder(
            ExpressionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BreastHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }
}