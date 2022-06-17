package com.intellisoft.nndak.charts

import com.google.gson.annotations.SerializedName

data class Statistics(
    @SerializedName("totalBabies") val totalBabies: String,
    @SerializedName("preterm") val preterm: String,
    @SerializedName("term") val term: String,
    @SerializedName("averageDays") val averageDays: String,
    @SerializedName("firstFeeding") val firstFeeding: FirstFeeding,
    @SerializedName("percentageFeeds") val percentageFeeds: PercentageFeeds,
    @SerializedName("mortalityRate") val mortalityRate: MortalityRate,
    @SerializedName("expressingTime") val expressingTime: List<ExpressingTime>

)

data class FirstFeeding(
    @SerializedName("withinOne") val withinOne: String,
    @SerializedName("afterOne") val afterOne: String,
    @SerializedName("afterTwo") val afterTwo: String,
    @SerializedName("afterThree") val afterThree: String,

    )

data class PercentageFeeds(
    @SerializedName("dhm") val dhm: String,
    @SerializedName("breastFeeding") val breastFeeding: String,
    @SerializedName("oral") val oral: String,
    @SerializedName("ebm") val ebm: String,
    @SerializedName("formula") val formula: String,

    )

data class MortalityRate(
    @SerializedName("rate") val rate: String,
    @SerializedName("data") val data: List<MortalityData>,
)

data class MortalityData(
    @SerializedName("month") val month: String,
    @SerializedName("value") val value: String,

    )

data class ExpressingTime(
    @SerializedName("month") val month: String,
    @SerializedName("underFive") val underFive: String,
    @SerializedName("underSeven") val underSeven: String,
    @SerializedName("aboveSeven") val aboveSeven: String,


    )

/**
 *DHM
 * */
data class DHMModel(
    @SerializedName("dhmInfants") val dhmInfants: String,
    @SerializedName("dhmVolume") val dhmVolume: String,
    @SerializedName("dhmAverage") val dhmAverage: String,
    @SerializedName("fullyReceiving") val fullyReceiving: String,
    @SerializedName("dhmLength") val dhmLength: String,
    @SerializedName("data") val data: List<DHMData>,

    )

data class DHMData(
    @SerializedName("day") val day: String,
    @SerializedName("preterm") val preterm: String,
    @SerializedName("term") val term: String,
    @SerializedName("total") val total: String,
)

