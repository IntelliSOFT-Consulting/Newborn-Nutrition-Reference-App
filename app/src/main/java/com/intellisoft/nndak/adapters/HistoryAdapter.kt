package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.charts.ActualData
import com.intellisoft.nndak.databinding.ItemWeightBinding
import com.intellisoft.nndak.holders.WeightHistoryViewHolder


class HistoryAdapter(
    private var babiesList: ArrayList<ActualData>,
    private val onItemClicked: (ActualData) -> Unit
) :
    ListAdapter<ActualData, WeightHistoryViewHolder>(ActualDataDiffCallback()) {

    var babyFilterList = ArrayList<ActualData>()

    init {
        babyFilterList = babiesList
    }

    class ActualDataDiffCallback : DiffUtil.ItemCallback<ActualData>() {
        override fun areItemsTheSame(
            oldItem: ActualData,
            newItem: ActualData
        ): Boolean = oldItem.date == newItem.date

        override fun areContentsTheSame(
            oldItem: ActualData,
            newItem: ActualData
        ): Boolean = oldItem.date == newItem.date
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightHistoryViewHolder {
        return WeightHistoryViewHolder(
            ItemWeightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: WeightHistoryViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }

    override fun getItemCount(): Int {
        return babyFilterList.size

    }

}