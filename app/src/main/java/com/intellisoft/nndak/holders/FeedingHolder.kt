package com.intellisoft.nndak.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.FeedingItemBinding
import com.intellisoft.nndak.models.PrescriptionItem

class FeedingHolder(binding: FeedingItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val appDate: TextView = binding.tvReference
    private val appTime: TextView = binding.tvTime
    private val appTotal: TextView = binding.totalTaken
    private val appStool: TextView = binding.tvStool
    private val appVomit: TextView = binding.tvVomit
    private val appDHM: TextView = binding.tvDhm
    private val appDiaper: TextView = binding.tvDiaper
    private val appIv: TextView = binding.appIvFluids
    private val appEBM: TextView = binding.appEbm
    private val appAdjust: TextView = binding.changer
    private val appDeficit: TextView = binding.deficit
    private val appRemarks: TextView = binding.remarks

    private val tvhIv: TextView = binding.tvhIv
    private val tvhEbm: TextView = binding.tvhEbm
    private val tvhDhm: TextView = binding.tvhDhm

    fun bindTo(data: PrescriptionItem) {
        this.appDate.text = data.date
        this.appTime.text = data.time
        this.appTotal.text = data.totalVolume
        this.appStool.text = data.consentDate
        this.appVomit.text = data.supplements
        this.appDHM.text = data.donorMilk
        this.appDiaper.text = data.route
        this.appIv.text = data.ivFluids
        this.appEBM.text = data.breastMilk
        this.appAdjust.text = data.dhmReason
        this.appDeficit.text = data.consent
        this.appRemarks.text = data.additionalFeeds

        if (data.donorMilk == "null") {
            this.appDHM.visibility = View.GONE
            this.tvhDhm.visibility = View.GONE
        }
        if (data.breastMilk == "null") {
            this.appEBM.visibility = View.GONE
            this.tvhEbm.visibility = View.GONE
        }
        if (data.ivFluids == "null") {
            this.appIv.visibility = View.GONE
            this.tvhIv.visibility = View.GONE
        }

    }
}