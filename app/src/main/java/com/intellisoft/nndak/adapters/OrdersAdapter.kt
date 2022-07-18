package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.charts.ItemOrder
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.databinding.OrderListViewBinding
import com.intellisoft.nndak.holders.BabyItemViewHolder
import com.intellisoft.nndak.holders.OrdersItemViewHolder
import com.intellisoft.nndak.models.OrdersItem

class OrdersAdapter(
    private var ordersList: ArrayList<ItemOrder>,
    private val onItemClicked: (ItemOrder) -> Unit
) :
    ListAdapter<ItemOrder, OrdersItemViewHolder>(OrdersItemDiffCallback()) {

    class OrdersItemDiffCallback : DiffUtil.ItemCallback<ItemOrder>() {
        override fun areItemsTheSame(
            oldItem: ItemOrder,
            newItem: ItemOrder
        ): Boolean = oldItem.orderId == newItem.orderId

        override fun areContentsTheSame(
            oldItem: ItemOrder,
            newItem: ItemOrder
        ): Boolean = oldItem.orderId == newItem.orderId
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