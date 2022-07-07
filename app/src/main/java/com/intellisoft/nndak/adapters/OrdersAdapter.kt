package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.databinding.OrderListViewBinding
import com.intellisoft.nndak.holders.BabyItemViewHolder
import com.intellisoft.nndak.holders.OrdersItemViewHolder
import com.intellisoft.nndak.models.OrdersItem

class OrdersAdapter(
    private var ordersList: ArrayList<OrdersItem>,
    private val onItemClicked: (OrdersItem) -> Unit
) :
    ListAdapter<OrdersItem, OrdersItemViewHolder>(OrdersItemDiffCallback()) {

    class OrdersItemDiffCallback : DiffUtil.ItemCallback<OrdersItem>() {
        override fun areItemsTheSame(
            oldItem: OrdersItem,
            newItem: OrdersItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: OrdersItem,
            newItem: OrdersItem
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersItemViewHolder {
        return OrdersItemViewHolder(
            OrderListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: OrdersItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)

    }
}