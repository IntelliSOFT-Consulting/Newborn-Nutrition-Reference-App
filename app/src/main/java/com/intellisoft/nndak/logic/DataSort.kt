package com.intellisoft.nndak.logic

import android.os.Build
import com.intellisoft.nndak.charts.CombinedGrowth
import com.intellisoft.nndak.charts.GrowthData
import com.intellisoft.nndak.charts.GrowthOptions
import com.intellisoft.nndak.charts.WeightsData
import com.intellisoft.nndak.helper_class.FormatHelper
import com.intellisoft.nndak.models.*
import java.time.LocalDate
import java.time.Period

class DataSort {
    companion object {


        fun sortCollected(data: List<ObservationItem>): List<ObservationItem> {

            return data.sortedWith(compareBy { it.effective })
        }


        fun sortHistory(data: List<FeedingHistory>): List<FeedingHistory> {

            return data.sortedWith(compareByDescending { it.hour })
        }

        fun sortPrescriptions(data: List<PrescriptionItem>): List<PrescriptionItem> {

            return data.sortedWith(compareBy { it.hour }).reversed()

        }


        fun extractDailyMeasure(entry: LocalDate, sorted: List<ObservationItem>): String {
       
            return sorted.findLast { FormatHelper().getSimpleDate(it.effective) == entry.toString() }?.quantity
                ?: sorted.find { FormatHelper().getSimpleDate(it.effective) == entry.toString() }?.quantity
                ?: "0.0"
        }

        fun getFormattedIntAge(
            dob: String
        ): Int {
            if (dob.isEmpty()) return 0
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Period.between(LocalDate.parse(dob), LocalDate.now()).let {
                    when {
                        it.years > 0 -> it.years
                        it.months > 0 -> it.months
                        else -> it.days
                    }
                }
            } else {
                0
            }
        }

        fun extractDaysData(
            totalDays: List<String>,
            values: WeightsData,
            growths: List<GrowthData>
        ): CombinedGrowth {
            val weights: MutableList<GrowthData> = mutableListOf()
            totalDays.forEach {
                val entry = findData(growths, it)
                weights.add(entry)
            }
            return CombinedGrowth(values, weights.flatMap { it.data.toList() })

        }

        private fun findData(growths: List<GrowthData>, day: String): GrowthData {
            return growths.find { it.age.toFloat() == day.toFloat() }!!
        }

          fun calculateGestationDays(values: WeightsData): List<String> {
            val babiesData = ArrayList<String>()
            babiesData.add(values.gestationAge)
            val weeklyGrowth = getWeekFromDays(values.dayOfLife)
            for (i in 0 until weeklyGrowth) {
                val addedDay = values.gestationAge.toFloat() + 1f
                babiesData.add(addedDay.toString())
            }
            return babiesData
        }

        private fun getWeekFromDays(dayOfLife: Int): Int {
            val divisor = 7

            val quotient = dayOfLife / divisor
            val remainder = dayOfLife % divisor

            return quotient

        }
          fun regulateProjection(data: List<GrowthOptions>): List<GrowthOptions> {
            return data.sortedWith(compareBy { it.value })
        }
    }
}