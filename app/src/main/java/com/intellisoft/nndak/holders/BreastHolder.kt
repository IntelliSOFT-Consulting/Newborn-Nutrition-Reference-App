package com.intellisoft.nndak.holders

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.ExpressionHistoryBinding
import com.intellisoft.nndak.models.BreastsHistory


class BreastHolder(binding: ExpressionHistoryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val tvhDate: TextView = binding.tvhDate
    private val tvhFrequency: TextView = binding.tvhFrequency
    private val tvhTiming: TextView = binding.tvhTiming
    private val tvhView: TextView = binding.tvhView
    private val lnParent: LinearLayout = binding.lnParent

    fun bindTo(
        data: BreastsHistory,
        onItemClicked: (BreastsHistory) -> Unit
    ) {
        this.tvhDate.visibility = ViewGroup.INVISIBLE
        this.tvhFrequency.text = data.date
        this.tvhTiming.text = "View Details"
        this.tvhTiming.setTextColor(ColorStateList.valueOf(Color.parseColor("#c62828")))

        this.tvhView.visibility = ViewGroup.INVISIBLE
        val seven: ViewGroup.LayoutParams =
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f)
        val three: ViewGroup.LayoutParams =
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.3f)
        this.lnParent.weightSum = 4f
        this.tvhDate.layoutParams = seven
        this.tvhFrequency.layoutParams = three
        this.tvhTiming.layoutParams = three
        this.tvhView.layoutParams = seven
        this.tvhTiming.setOnClickListener {
            onItemClicked(data)
        }
    }
}