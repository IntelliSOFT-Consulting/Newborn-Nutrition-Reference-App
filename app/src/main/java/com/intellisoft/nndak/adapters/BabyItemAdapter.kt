package com.intellisoft.nndak.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.intellisoft.nndak.databinding.BabyListItemViewBinding
import com.intellisoft.nndak.holders.BabyItemViewHolder
import com.intellisoft.nndak.models.MotherBabyItem
import java.util.*
import kotlin.collections.ArrayList

class BabyItemAdapter(
    private var babiesList: ArrayList<MotherBabyItem>,
    private val onItemClicked: (MotherBabyItem) -> Unit
) :
    ListAdapter<MotherBabyItem, BabyItemViewHolder>(MotherBabyItemDiffCallback()), Filterable {

    var babyFilterList = ArrayList<MotherBabyItem>()

    init {
        babyFilterList = babiesList
    }

    class MotherBabyItemDiffCallback : DiffUtil.ItemCallback<MotherBabyItem>() {
        override fun areItemsTheSame(
            oldItem: MotherBabyItem,
            newItem: MotherBabyItem
        ): Boolean = oldItem.resourceId == newItem.resourceId

        override fun areContentsTheSame(
            oldItem: MotherBabyItem,
            newItem: MotherBabyItem
        ): Boolean = oldItem.id == newItem.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BabyItemViewHolder {
        return BabyItemViewHolder(
            BabyListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BabyItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bindTo(item, onItemClicked)
    }

    override fun getItemCount(): Int {
        return babyFilterList.size

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    babyFilterList = babiesList
                } else {
                    val resultList = ArrayList<MotherBabyItem>()
                    for (row in babiesList) {
                        if (row.babyName.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)) || row.motherName.lowercase(
                                Locale.ROOT
                            )
                                .contains(charSearch.lowercase(Locale.ROOT)) || row.motherIp.lowercase(
                                Locale.ROOT
                            )
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    babyFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = babyFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                babyFilterList = results?.values as ArrayList<MotherBabyItem>
                notifyDataSetChanged()
            }

        }
    }
}