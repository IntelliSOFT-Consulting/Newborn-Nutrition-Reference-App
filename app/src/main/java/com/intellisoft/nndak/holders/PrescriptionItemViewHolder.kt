package com.intellisoft.nndak.holders

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.PrescriptionListItemBinding
import com.intellisoft.nndak.models.PrescriptionItem

class PrescriptionItemViewHolder(binding: PrescriptionListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val appDate: TextView = binding.appDate
    private val appTime: TextView = binding.appTime
    private val appTotalFeedVolume: TextView = binding.appTotalFeedVolume
    private val appRoute: TextView = binding.appRoute
    private val appFrequency: TextView = binding.appFrequency
    private val appIvFluids: TextView = binding.appIvFluids
    private val appBreastMilk: TextView = binding.appBreastMilk
    private val appDhm: TextView = binding.appDhm
    private val appConsent: TextView = binding.appConsent
    private val appConsentDate: TextView = binding.appConsentDate
    private val appDhmReason: TextView = binding.appDhmReason
    private val appAdditionalFeeds: TextView = binding.appAdditionalFeeds
    private val appFeedingSupplements: TextView = binding.appFeedingSupplements

    fun bindTo(
        patientItem: PrescriptionItem,
        onItemClicked: (PrescriptionItem) -> Unit
    ) {
        this.appDate.text = patientItem.date
        this.appTime.text = patientItem.time
        this.appTotalFeedVolume.text = patientItem.totalVolume
        this.appRoute.text = patientItem.route
        this.appFrequency.text = patientItem.frequency
        this.appIvFluids.text = patientItem.ivFluids
        this.appBreastMilk.text = patientItem.breastMilk
        this.appDhm.text = patientItem.donorMilk
        this.appConsent.text = patientItem.consent
        this.appConsentDate.text = patientItem.consentDate
        this.appDhmReason.text = patientItem.dhmReason
        this.appAdditionalFeeds.text = patientItem.additionalFeeds
        this.appFeedingSupplements.text = patientItem.supplements
        this.itemView.setOnClickListener { onItemClicked(patientItem) }


    }
}