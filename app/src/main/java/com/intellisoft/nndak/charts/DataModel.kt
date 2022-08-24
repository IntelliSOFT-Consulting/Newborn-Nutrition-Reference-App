package com.intellisoft.nndak.charts

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.intellisoft.nndak.data.Stock

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
    @SerializedName("iv") val iv: String,
    @SerializedName("oral") val oral: String,
    @SerializedName("ebm") val ebm: String,
    @SerializedName("formula") val formula: String,
)

data class MortalityRate(
    @SerializedName("rate") val rate: String,
    @SerializedName("data") val data: List<Data>,
)

data class Data(
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
    @SerializedName("dhmVolume") val dhmVolume: DHMCategory,
    @SerializedName("dhmAverage") val dhmAverage: String,
    @SerializedName("fullyReceiving") val fullyReceiving: String,
    @SerializedName("dhmLength") val dhmLength: String,
    @SerializedName("data") val data: List<DHMData>,
)

data class DHMCategory(
    @SerializedName("preterm") val preterm: Stock,
    @SerializedName("term") val term: Stock,
)
data class DHMData(
    @SerializedName("day") val day: String,
    @SerializedName("preterm") val preterm: Stock,
    @SerializedName("term") val term: Stock,
)

data class MilkExpression(
    @SerializedName("totalAmount") val totalFeed: String,
    @SerializedName("varianceAmount") val varianceAmount: String,
    @SerializedName("data") val data: List<ExpressionData>,
)

data class ExpressionData(
    @SerializedName("time") val time: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("number") val number: String,
)

data class FeedsDistribution(
    @SerializedName("totalFeed") val totalFeed: String,
    @SerializedName("varianceAmount") val varianceAmount: String,
    @SerializedName("data") val data: List<FeedsData>,
)

data class FeedsData(
    @SerializedName("time") val time: String,
    @SerializedName("breastVolume") val breastVolume: String,
    @SerializedName("ivVolume") val ivVolume: String,
    @SerializedName("ebmVolume") val ebmVolume: String,
    @SerializedName("dhmVolume") val dhmVolume: String,
    @SerializedName("formula") val formula: String,
)

data class WeightsData(
    @SerializedName("babyGender") val babyGender: String,
    @SerializedName("currentWeight") val currentWeight: String,
    @SerializedName("gestationAge") val gestationAge: String,
    @SerializedName("dayOfLife") val dayOfLife: Int,
    @SerializedName("weeksLife") val weeksLife: Int,
    @SerializedName("data") val data: List<ActualData>,
)

data class ActualData(
    @SerializedName("lifeDay") val day: Int,
    @SerializedName("actual") val actual: String,
    @SerializedName("projected") val projected: String,
)


data class OrderData(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<ItemOrder>,
)

data class ItemOrder(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("patientId") val patientId: String,
    @SerializedName("motherIp") val motherIp: String,
    @SerializedName("motherName") val motherName: String,
    @SerializedName("babyName") val babyName: String,
    @SerializedName("babyAge") val babyAge: String,
    @SerializedName("consentGiven") val consentGiven: String,
    @SerializedName("dhmType") val dhmType: String,
    @SerializedName("dhmReason") val dhmReason: String,
    @SerializedName("dhmVolume") val dhmVolume: String,
)

data class CombinedGrowth(
    val actualWeight: WeightsData,
    val projectedWeight: List<GrowthOptions>
)


data class GrowthData(
    val age: Int,
    val data: List<GrowthOptions>
)

data class GrowthOptions(
    val option: String,
    val value: String,
)