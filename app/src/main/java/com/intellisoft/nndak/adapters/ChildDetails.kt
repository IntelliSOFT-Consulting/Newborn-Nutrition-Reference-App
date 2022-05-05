package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.PatientDetailsCardViewBinding
import com.intellisoft.nndak.databinding.PatientDetailsHeaderBinding
import com.intellisoft.nndak.databinding.PatientListItemViewBinding
import com.intellisoft.nndak.utils.*
import com.intellisoft.nndak.viewmodels.*

class ChildDetails(val show: (Boolean)) :
    ListAdapter<PatientDetailData, PatientDetailItemViewHolder>(PatientDetailDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientDetailItemViewHolder {
        return when (ViewTypes.from(viewType)) {
            ViewTypes.HEADER ->
                PatientDetailsHeaderItemViewHolder(
                    PatientDetailsCardViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ViewTypes.PATIENT ->

                RelatedOverviewItemViewHolder(
                    PatientDetailsHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), show

                )

            ViewTypes.PATIENT_PROPERTY ->
                PatientPropertyItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            ViewTypes.OBSERVATION ->
                PatientDetailsObservationItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ViewTypes.CONDITION ->
                PatientDetailsConditionItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: PatientDetailItemViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
        if (holder is PatientDetailsHeaderItemViewHolder) return

        holder.itemView.background =
            if (model.firstInGroup && model.lastInGroup) {
                allCornersRounded()
            } else if (model.firstInGroup) {
                topCornersRounded()
            } else if (model.lastInGroup) {
                bottomCornersRounded()
            } else {
                noCornersRounded()
            }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            is PatientDetailHeader -> ViewTypes.HEADER
            is PatientDetailOverview -> ViewTypes.PATIENT
            is PatientDetailProperty -> ViewTypes.PATIENT_PROPERTY
            is PatientDetailObservation -> ViewTypes.OBSERVATION
            is PatientDetailCondition -> ViewTypes.CONDITION
            else -> {
                throw IllegalArgumentException("Undefined Item type")
            }
        }.ordinal
    }
}