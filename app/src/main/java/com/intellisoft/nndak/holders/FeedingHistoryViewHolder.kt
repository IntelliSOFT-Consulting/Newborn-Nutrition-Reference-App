package com.intellisoft.nndak.holders

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.FeedingHistoryItemBinding
import com.intellisoft.nndak.models.FeedingHistory


class FeedingHistoryViewHolder(binding: FeedingHistoryItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val tvhDate: TextView = binding.tvhDate
    private val tvhTime: TextView = binding.tvhTime
    private val tvhEbm: TextView = binding.tvhEbm
    private val tvhDhm: TextView = binding.tvhDhm
    private val tvhIv: TextView = binding.tvhIv
    private val tvhDeficit: TextView = binding.tvhDeficit
    private val tvhVomit: TextView = binding.tvhVomit
    private val tvhDiaper: TextView = binding.tvhDiaper
    private val tvhStool: TextView = binding.tvhStool

    fun bindTo(data: FeedingHistory) {
        this.tvhDate.text = data.date
        this.tvhTime.text = data.time
        this.tvhEbm.text = data.ebm
        this.tvhDhm.text = data.dhm
        this.tvhIv.text = data.iv
        this.tvhDeficit.text = data.deficit
        this.tvhVomit.text = data.vomit
        this.tvhDiaper.text = data.diaper
        this.tvhStool.text = data.stool
    }
}