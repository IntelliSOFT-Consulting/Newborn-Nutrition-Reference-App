package com.intellisoft.nndak.charts

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat


class ChartFormatter : ValueFormatter() {
    private val mFormat: DecimalFormat = DecimalFormat("###,###,##0.0")
    override fun getFormattedValue(value: Float): String {
        return mFormat.format(value).toString() + " %"
    }

    init {
        // use one decimal
    }
}