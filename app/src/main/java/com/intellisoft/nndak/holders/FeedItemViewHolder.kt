package com.intellisoft.nndak.holders

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.intellisoft.nndak.R
import com.intellisoft.nndak.databinding.FeedItemViewBinding
import com.intellisoft.nndak.databinding.ItemBinding
import com.intellisoft.nndak.models.FeedItem

class FeedItemViewHolder(binding: ItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val matType: MaterialButton = binding.name

    fun bindTo(
        item: FeedItem,
        onItemClicked: (FeedItem) -> Unit
    ) {
        this.matType.text = item.type
        if (item.id == "0") {
            this.matType.background.setTint((Color.parseColor("#24A047")))
            this.matType.setOnClickListener {
                onItemClicked(item)
            }
        }

    }
}