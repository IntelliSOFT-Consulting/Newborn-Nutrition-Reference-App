package com.intellisoft.nndak.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.PatientDetailsCardViewBinding
import com.intellisoft.nndak.databinding.PatientDetailsHeaderBinding
import com.intellisoft.nndak.databinding.PatientListItemViewBinding
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.utils.*
import com.intellisoft.nndak.viewmodels.*

class MaternityDetails(
    private val onScreenerClick: (RelatedPersonItem) -> Unit
) :

    ListAdapter<PatientDetailData, PatientDetailItemViewHolder>(PatientDetailDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientDetailItemViewHolder {
        return when (ViewType.from(viewType)) {
            ViewType.HEADER ->
                PatientDetailsHeaderItemViewHolder(
                    PatientDetailsCardViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ViewType.PATIENT ->
                OverviewItemViewHolder(
                    PatientDetailsHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),

                    )
            ViewType.CHILD ->
                ChildOverviewItemViewHolder(
                    PatientDetailsHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),

                    )
            ViewType.PATIENT_PROPERTY ->
                PatientPropertyItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            /***
             * Add option to display related persons
             * */
            ViewType.RELATION ->
                PatientDetailsRelationItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onScreenerClick
                )
            ViewType.OBSERVATION ->
                PatientDetailsObservationItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ViewType.CONDITION ->
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
            is PatientDetailHeader -> ViewType.HEADER
            is PatientDetailOverview -> ViewType.PATIENT
            is ChildDetailOverview -> ViewType.CHILD
            is PatientDetailProperty -> ViewType.PATIENT_PROPERTY
            is PatientDetailRelation -> ViewType.RELATION
            is PatientDetailObservation -> ViewType.OBSERVATION
            is PatientDetailCondition -> ViewType.CONDITION
            else -> {
                throw IllegalArgumentException("Undefined Item type")
            }
        }.ordinal
    }
}

class OverviewItemViewHolder(
    private val binding: PatientDetailsHeaderBinding,
) : PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as PatientDetailOverview).let { binding.title.text = it.patient.name }
        data.patient.riskItem?.let {
            binding.patientContainer.setBackgroundColor(it.patientCardColor)
            binding.statusValue.text = it.riskStatus
            binding.statusValue.setTextColor(Color.BLACK)
            binding.statusValue.background =
                allCornersRounded().apply { fillColor = ColorStateList.valueOf(it.riskStatusColor) }
            binding.lastContactValue.text = it.lastContacted
        }
    }
}


/**
 * Child Overview
 **/
class ChildOverviewItemViewHolder(
    private val binding: PatientDetailsHeaderBinding,
) : PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as ChildDetailOverview).let {
            binding.title.text = it.relation.name

        }
        data.relation.riskItem?.let {
            binding.patientContainer.setBackgroundColor(it.patientCardColor)
            binding.statusValue.text = it.riskStatus
            binding.statusValue.setTextColor(Color.BLACK)
            binding.statusValue.background =
                allCornersRounded().apply { fillColor = ColorStateList.valueOf(it.riskStatusColor) }
            binding.lastContactValue.text = it.lastContacted

        }
    }
}

class PatientDetailsRelationItemViewHolder(
    private val binding: PatientListItemViewBinding,
    val viewChildClick: (RelatedPersonItem) -> Unit
) :
    PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {

        (data as PatientDetailRelation).let {

            binding.name.text = it.relation.name
            binding.fieldName.text = it.relation.dob
            binding.tvView.setOnClickListener { viewChildClick(data.relation) }

        }

        binding.status.visibility = View.GONE
        binding.id.visibility = View.GONE
        binding.tvView.visibility = View.VISIBLE
    }
}