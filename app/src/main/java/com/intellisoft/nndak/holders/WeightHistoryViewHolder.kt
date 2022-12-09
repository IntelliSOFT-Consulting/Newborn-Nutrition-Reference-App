package com.intellisoft.nndak.holders

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.charts.ActualData
import com.intellisoft.nndak.databinding.ItemWeightBinding
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat

class WeightHistoryViewHolder(binding: ItemWeightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val tvDate: TextView = binding.tvDate
    private val tvWeight: TextView = binding.tvWeight
    private val tvScore: TextView = binding.tvScore
    fun bindTo(
        patientItem: ActualData,
        onItemClicked: (ActualData) -> Unit
    ) {

        //limit to 2 decimal places
        try {
            val actual = patientItem.actual.toFloat()
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            val weight = df.format(actual)
            this.tvWeight.text = weight
        } catch (e: Exception) {
            this.tvWeight.text = patientItem.actual
        }
        this.tvDate.text = patientItem.date
        this.tvScore.text = ""

    }

}
