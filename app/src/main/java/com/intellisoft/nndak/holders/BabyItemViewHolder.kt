package com.intellisoft.nndak.holders

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat.getColor
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.models.MotherBabyItem
import com.intellisoft.nndak.models.PatientItem
import java.time.LocalDate
import java.time.Period

class BabyItemViewHolder(binding: BabyListItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val appBabyName: TextView = binding.appBabyName
    private val appMotherName: TextView = binding.appMotherName
    private val appIpNumber: TextView = binding.appIpNumber
    private val appBirthWeight: TextView = binding.appBirthWeight
    private val appStatus: TextView = binding.appStatus
    private val appRateGain: TextView = binding.appRateGain

    fun bindTo(
        patientItem: MotherBabyItem,
        onItemClicked: (MotherBabyItem) -> Unit
    ) {
        this.appBabyName.text = patientItem.babyName
        this.appMotherName.text = patientItem.motherName
        this.appBirthWeight.text = patientItem.birthWeight
        this.appStatus.text = patientItem.status
        this.appRateGain.text = patientItem.gainRate
        this.appIpNumber.text = patientItem.motherIp  //"#---${getTruncatedId(patientItem)}"
        this.itemView.setOnClickListener { onItemClicked(patientItem) }

        if (patientItem.status == "Term") {
            this.appStatus.setTextColor(ColorStateList.valueOf(Color.parseColor("#24A047")))
        }
        if (patientItem.gainRate == "Low") {
            this.appRateGain.setTextColor(ColorStateList.valueOf(Color.parseColor("#c62828")))
        }

    }


    private fun getTruncatedId(patientItem: MotherBabyItem): String {
        return patientItem.resourceId.takeLast(3)
    }
}
