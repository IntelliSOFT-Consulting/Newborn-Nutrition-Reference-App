package com.intellisoft.nndak.logic


/** Validates a single feed from the quick feed entry form before it is saved. */
object FeedValidator {

    const val MAX_FEED_VOLUME_ML = 200.0

    data class Result(val isValid: Boolean, val message: String? = null)

    fun validate(volumeText: String, feedType: String, babyWeightGrams: Int?): Result {
        if (feedType.isBlank()) {
            return Result(false, "Please select a feed type")
        }

        val volume = volumeText.trim().toDoubleOrNull()
            ?: return Result(false, "Enter the volume in mls")

        if (volume > MAX_FEED_VOLUME_ML) {
            return Result(false, "Volume cannot exceed $MAX_FEED_VOLUME_ML mls")
        }

        return Result(true)
    }
}