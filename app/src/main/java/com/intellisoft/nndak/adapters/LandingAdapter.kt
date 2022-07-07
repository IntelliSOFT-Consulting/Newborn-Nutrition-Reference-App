package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.ItemLandingBinding
import com.intellisoft.nndak.viewmodels.LayoutListViewModel


class LandingAdapter(private val onItemClick: (LayoutListViewModel.Layout) -> Unit) :
    ListAdapter<LayoutListViewModel.Layout, LayoutViewHolder>(LayoutDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayoutViewHolder {
        return LayoutViewHolder(
            ItemLandingBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemClick
        )
    }

    override fun onBindViewHolder(holder: LayoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LayoutViewHolder(
    val binding: ItemLandingBinding,
    private val onItemClick: (LayoutListViewModel.Layout) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(layout: LayoutListViewModel.Layout) {
        binding.imgDashboard.setImageResource(layout.iconId)
        binding.tvLabel.text =
            binding.tvLabel.context.getString(layout.textId)
        binding.root.setOnClickListener { onItemClick(layout) }
    }
}

class LayoutDiffUtil : DiffUtil.ItemCallback<LayoutListViewModel.Layout>() {
    override fun areItemsTheSame(
        oldLayout: LayoutListViewModel.Layout,
        newLayout: LayoutListViewModel.Layout
    ) = oldLayout === newLayout

    override fun areContentsTheSame(
        oldLayout: LayoutListViewModel.Layout,
        newLayout: LayoutListViewModel.Layout
    ) = oldLayout == newLayout
}
