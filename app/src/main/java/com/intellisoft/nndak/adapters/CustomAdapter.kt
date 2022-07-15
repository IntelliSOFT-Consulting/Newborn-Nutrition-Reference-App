package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.ItemBinding
import com.intellisoft.nndak.models.FeedItem
import com.intellisoft.nndak.models.ScheduleTime

class CustomAdapter(
    private val heroList: ArrayList<ScheduleTime>,
    private val listener: (ScheduleTime, Int) -> Unit

) : /*RecyclerView.Adapter<CustomAdapter.ViewHolder>() {*/
    ListAdapter<ScheduleTime, CustomAdapter.ViewHolder>(CustomAdapterDiffCallback()) {
    class CustomAdapterDiffCallback : DiffUtil.ItemCallback<ScheduleTime>() {
        override fun areItemsTheSame(
            oldItem: ScheduleTime,
            newItem: ScheduleTime
        ): Boolean = oldItem.hour == newItem.hour

        override fun areContentsTheSame(
            oldItem: ScheduleTime,
            newItem: ScheduleTime
        ): Boolean = oldItem.hour == newItem.hour
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(heroList[position])
        holder.itemView.setOnClickListener { listener(heroList[position], position) }
    }

    override fun getItemCount(): Int {
        return heroList.size
    }

    class ViewHolder(var itemBinding: ItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(hero: ScheduleTime) {
            itemBinding.name.text = hero.hour
        }
    }

}