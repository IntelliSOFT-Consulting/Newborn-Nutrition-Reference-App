
package com.imeja.demo.holders

import android.content.res.ColorStateList
import android.content.res.Resources
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.imeja.demo.R
import com.imeja.demo.databinding.PatientListItemViewBinding
import com.imeja.demo.models.PatientItem
import com.imeja.demo.viewmodels.PatientListViewModel
import java.time.LocalDate
import java.time.Period
import org.hl7.fhir.r4.model.codesystems.RiskProbability

class PatientItemViewHolder(binding: PatientListItemViewBinding) :
  RecyclerView.ViewHolder(binding.root) {
  private val statusView: ImageView = binding.status
  private val nameView: TextView = binding.name
  private val ageView: TextView = binding.fieldName
  private val idView: TextView = binding.id

  fun bindTo(
      patientItem: PatientItem,
      onItemClicked: (PatientItem) -> Unit
  ) {
    this.nameView.text = patientItem.name
    this.ageView.text = getFormattedAge(patientItem, ageView.context.resources)
    this.idView.text = "Id: #---${getTruncatedId(patientItem)}"
    this.itemView.setOnClickListener { onItemClicked(patientItem) }
    statusView.imageTintList =
      ColorStateList.valueOf(
        ContextCompat.getColor(
          statusView.context,
          when (patientItem.risk) {
            RiskProbability.HIGH.toCode() -> R.color.high_risk
            RiskProbability.MODERATE.toCode() -> R.color.moderate_risk
            RiskProbability.LOW.toCode() -> R.color.low_risk
            else -> R.color.unknown_risk
          }
        )
      )
  }

  private fun getFormattedAge(
      patientItem: PatientItem,
      resources: Resources
  ): String {
    if (patientItem.dob.isEmpty()) return ""
    return Period.between(LocalDate.parse(patientItem.dob), LocalDate.now()).let {
      when {
        it.years > 0 -> resources.getQuantityString(R.plurals.ageYear, it.years, it.years)
        it.months > 0 -> resources.getQuantityString(R.plurals.ageMonth, it.months, it.months)
        else -> resources.getQuantityString(R.plurals.ageDay, it.days, it.days)
      }
    }
  }

  /** The new ui just shows shortened id with just last 3 characters. */
  private fun getTruncatedId(patientItem: PatientItem): String {
    return patientItem.resourceId.takeLast(3)
  }
}
