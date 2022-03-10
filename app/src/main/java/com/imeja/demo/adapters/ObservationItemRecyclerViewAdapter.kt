
package com.imeja.demo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.imeja.demo.holders.ObservationItemViewHolder
import com.imeja.demo.databinding.ObservationListItemBinding
import com.imeja.demo.models.ObservationItem
import com.imeja.demo.viewmodels.PatientListViewModel

/** UI Controller helper class to display list of observations. */
class ObservationItemRecyclerViewAdapter :
  ListAdapter<ObservationItem, ObservationItemViewHolder>(
    ObservationItemDiffCallback()
  ) {

  class ObservationItemDiffCallback :
    DiffUtil.ItemCallback<ObservationItem>() {
    override fun areItemsTheSame(
        oldItem: ObservationItem,
        newItem: ObservationItem
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ObservationItem,
        newItem: ObservationItem
    ): Boolean = oldItem.id == newItem.id
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationItemViewHolder {
    return ObservationItemViewHolder(
      ObservationListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
  }

  override fun onBindViewHolder(holder: ObservationItemViewHolder, position: Int) {
    val item = currentList[position]
    holder.bindTo(item)
  }
}
