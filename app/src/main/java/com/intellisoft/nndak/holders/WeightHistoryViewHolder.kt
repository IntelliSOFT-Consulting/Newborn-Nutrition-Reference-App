package com.intellisoft.nndak.holders

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.charts.ActualData
import com.intellisoft.nndak.databinding.ItemWeightBinding

class WeightHistoryViewHolder(binding: ItemWeightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val tvDate: TextView = binding.tvDate
    private val tvWeight: TextView = binding.tvWeight
    private val tvScore: TextView = binding.tvScore
    fun bindTo(
        patientItem: ActualData,
        onItemClicked: (ActualData) -> Unit
    ) {
        this.tvDate.text = patientItem.date
        this.tvWeight.text = patientItem.actual
        this.tvScore.text = ""

    }

}
