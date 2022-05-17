package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.EncounterListItemViewBinding
import com.intellisoft.nndak.databinding.PatientDetailsCardViewBinding
import com.intellisoft.nndak.databinding.PatientDetailsHeaderBinding
import com.intellisoft.nndak.databinding.PatientListItemViewBinding
import com.intellisoft.nndak.models.EncounterItem
import com.intellisoft.nndak.models.RelatedPersonItem
import com.intellisoft.nndak.models.Steps
import com.intellisoft.nndak.utils.*
import com.intellisoft.nndak.viewmodels.*

class MaternityDetails(
    private val onScreenerClick: (RelatedPersonItem) -> Unit,
    private val newBornClick: () -> Unit,
    private val maternityClick: () -> Unit,
    private val encounterClick: (EncounterItem) -> Unit,
    private val steps: (Steps),
    val show: (Boolean),
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
                if (show) {
                    OverviewItemViewHolder(
                        PatientDetailsHeaderBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        ), newBornClick, maternityClick, steps, show

                    )
                } else {
                    RelatedOverviewItemViewHolder(
                        PatientDetailsHeaderBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        ), show

                    )
                }
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
            ViewType.ENCOUNTER ->
                PatientDetailsEncounterItemViewHolder(
                    EncounterListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), encounterClick
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
        if (holder is PatientDetailsEncounterItemViewHolder) return

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
            is PatientDetailEncounter -> ViewType.ENCOUNTER
            else -> {
                throw IllegalArgumentException("Undefined Item type")
            }
        }.ordinal
    }
}
