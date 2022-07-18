package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.ExpressionHistoryBinding
import com.intellisoft.nndak.holders.ExpressionHolder
import com.intellisoft.nndak.models.ExpressionHistory

class ExpressionAdapter(
    private val onItemClicked: (ExpressionHistory) -> Unit
): ListAdapter<ExpressionHistory, ExpressionHolder>(PrescriptionItemDiffCallback()) {

    class PrescriptionItemDiffCallback : DiffUtil.ItemCallback<ExpressionHistory>() {
        override fun areItemsTheSame(
            oldItem: ExpressionHistory,
            newItem: ExpressionHistory
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: ExpressionHistory,
            newItem: ExpressionHistory
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpressionHolder {
        return ExpressionHolder(
            ExpressionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ExpressionHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item,onItemClicked)
    }
}