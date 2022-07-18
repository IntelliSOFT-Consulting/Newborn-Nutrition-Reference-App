package com.intellisoft.nndak.holders

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.ExpressionHistoryBinding
import com.intellisoft.nndak.models.ExpressionHistory


class ExpressionHolder(binding: ExpressionHistoryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val tvhDate: TextView = binding.tvhDate
    private val tvhFrequency: TextView = binding.tvhFrequency
    private val tvhTiming: TextView = binding.tvhTiming
    private val tvhView: TextView = binding.tvhView
    private val lnParent: LinearLayout = binding.lnParent

    fun bindTo(
        data: ExpressionHistory,
        onItemClicked: (ExpressionHistory) -> Unit
    ) {
        this.tvhDate.text = data.date
        this.tvhFrequency.text = data.frequency
        this.tvhTiming.text = data.timing
        this.tvhView.text = "View Details"
        val seven: ViewGroup.LayoutParams =
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f)
        val three: ViewGroup.LayoutParams =
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.3f)
        this.lnParent.weightSum = 4f
        this.tvhDate.layoutParams = seven
        this.tvhFrequency.layoutParams = three
        this.tvhTiming.layoutParams = three
        this.tvhView.layoutParams = seven
        this.tvhView.setOnClickListener {
            onItemClicked(data)
        }
    }
}