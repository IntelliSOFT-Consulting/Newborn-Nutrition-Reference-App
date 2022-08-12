package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.charts.ItemOrder
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.databinding.OrderListViewBinding
import com.intellisoft.nndak.holders.BabyItemViewHolder
import com.intellisoft.nndak.holders.OrdersItemViewHolder
import com.intellisoft.nndak.models.OrdersItem
import java.util.*
import kotlin.collections.ArrayList

class OrdersAdapter(
    private var ordersList: ArrayList<ItemOrder>,
    private val onItemClicked: (ItemOrder) -> Unit
) :
    ListAdapter<ItemOrder, OrdersItemViewHolder>(OrdersItemDiffCallback()), Filterable {

    var ordersFilterList = ArrayList<ItemOrder>()

    init {
        ordersFilterList = ordersList
    }

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

    override fun getItemCount(): Int {
        return ordersFilterList.size

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    ordersFilterList = ordersList
                } else {
                    val resultList = ArrayList<ItemOrder>()
                    for (row in ordersList) {
                        if (row.motherName.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                            || row.babyName.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                            || row.dhmType.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                            || row.consentGiven.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    ordersFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = ordersFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                ordersFilterList = results?.values as ArrayList<ItemOrder>
                notifyDataSetChanged()
            }

        }
    }
}