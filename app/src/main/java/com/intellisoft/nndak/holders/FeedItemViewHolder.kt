package com.intellisoft.nndak.holders

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.nndak.databinding.FeedItemViewBinding
import com.intellisoft.nndak.models.FeedItem

class FeedItemViewHolder(binding: FeedItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val matType: TextView = binding.matType


    fun bindTo(
        item: FeedItem,
        onItemClicked: (FeedItem) -> Unit
    ) {
        this.matType.text = item.type


    }
}