package com.imeja.demo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.imeja.demo.holders.PatientItemViewHolder
import com.imeja.demo.databinding.PatientListItemViewBinding
import com.imeja.demo.models.PatientItem
import com.imeja.demo.viewmodels.PatientListViewModel

/** UI Controller helper class to monitor Patient viewmodel and display list of patients. */
class PatientItemRecyclerViewAdapter(
    private val onItemClicked: (PatientItem) -> Unit
) :
    ListAdapter<PatientItem, PatientItemViewHolder>(PatientItemDiffCallback()) {

    class PatientItemDiffCallback : DiffUtil.ItemCallback<PatientItem>() {
        override fun areItemsTheSame(
            oldItem: PatientItem,
            newItem: PatientItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: PatientItem,
            newItem: PatientItem
        ): Boolean = oldItem.id == newItem.id && oldItem.risk == newItem.risk
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientItemViewHolder {
        return PatientItemViewHolder(
            PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PatientItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }
}
