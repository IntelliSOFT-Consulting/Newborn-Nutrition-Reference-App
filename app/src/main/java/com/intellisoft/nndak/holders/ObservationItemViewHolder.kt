package com.intellisoft.nndak.holders

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.ObservationListItemBinding
import com.intellisoft.nndak.models.ObservationItem

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
