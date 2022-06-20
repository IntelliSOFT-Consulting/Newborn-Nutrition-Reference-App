package com.intellisoft.nndak.charts

import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry


internal class ColorFormatter(yVals: List<BarEntry?>, label: String) :
    BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry): Int {
        return 0
    }

    override fun getColor(index: Int): Int {
        return if (getEntryForIndex(index).y < 30) mColors[0]
        else if (getEntryForIndex(index).y < 99) mColors[1] else mColors[2]
    }
}