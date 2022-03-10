package com.imeja.demo.holders

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.imeja.demo.R
import com.imeja.demo.databinding.ObservationListItemBinding
import com.imeja.demo.models.ObservationItem
import com.imeja.demo.viewmodels.PatientListViewModel

class ObservationItemViewHolder(private val binding: ObservationListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val observationTextView: TextView = binding.observationDetail

    fun bindTo(observationItem: ObservationItem) {
        this.observationTextView.text =
            itemView.resources.getString(
                R.string.observation_brief_text,
                observationItem.code,
                observationItem.value,
                observationItem.effective
            )
    }
}
