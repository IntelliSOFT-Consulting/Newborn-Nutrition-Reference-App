package com.intellisoft.nndak.logic

import org.hl7.fhir.r4.model.codesystems.ListOrder

class Logics {

    companion object {
        private const val ASTHMA = "161527007"
        private const val LUNG_DISEASE = "13645005"
        private const val DEPRESSION = "35489007"
        private const val DIABETES = "161445009"
        private const val HYPER_TENSION = "161501007"
        private const val HEART_DISEASE = "56265001"
        private const val HIGH_BLOOD_LIPIDS = "161450003"
        private const val FEVER = "386661006"
        private const val SHORTNESS_BREATH = "13645005"
        private const val COUGH = "49727002"
        private const val LOSS_OF_SMELL = "44169009"

        /**
         * Common Section
         */
        private const val COMPLETED_BY = "Completed By"

        /**
         * Mother's Assessment
         */
        private const val LACTATION_SUPPORT = "Lactation Support Required"
        private const val CONTRAINDICATED = "Contraindicated"
        private const val BM_THROUGH_BF = "Provide BM through BF"
        private const val BREATHING_DIFFICULTY = "Difficulty Feeding"

        /**
         * New Born Unit- Mothers Details
         */
        private const val MOTHER_MEDICAL = "Mother's Medical Condition"
        private const val ANTENATAL = "Antenatal Corticosteroids"

        /**
         * Lactation Assessment
         */
        private const val LACTATION_SUPPORT_TIME = "Time of Lactation Support"
        private const val PROPER_POSITIONING = "Proper Positioning"
        private const val NIPPLE_STIMULATION = "Nipple Stimulation"
        private const val EARLY_LICKING = "Early Licking"
        private const val MANUAL_EXPRESSION = "Manual Expression"
        private const val AWAKENING_BABY = "Awakening Baby"
        private const val SUPPORT_SUCCESSFUL = "Lactation Support Successful"
        private const val EFFECTIVE_EXPRESSION = "Effectively Expressing Milk"
        private const val ADDITIONAL_COMMENTS = "Additional Comments"

        /**
         * Milk Expression
         */

        private const val AMOUNT_EXPRESSED = "Amount Expressed"
        private const val EXPRESSION_TIME = "Time of Expression"
        private const val STORAGE_TIME = "Time of Storage"

        /**
         * Child Feeding
         */
        private const val FEEDING_CUES = "Feeding Readiness Cues"
        private const val LATCHES = "Latches"
        private const val STEADY_SUCK = "Sucks Steadily"
        private const val AUDIBLE_SWALLOW = "Swallows Audibly"
        private const val NO_CHOCKING = "Feeds Without Chocking"
        private const val BREAST_SOFTENING = "Breasts Softening"
        private const val TIME_PER_SIDE = "10 mins per side"
        private const val COMFORTABLE_SLEEP = "Sleeps Comfortably"
        private const val WET_DIAPER = "6-8 Wet Diaper"
        private const val MORE_WEIGHT_LOSS = "Loses no more than 10% of birth weight"
        private const val EFFICIENT_BF = "Breast Feeding Efficient"
        private const val SUFFICIENT_BF = "Breast Feeding Sufficient"

        /**
         * Child Supplements
         */

        private const val SUPPLEMENTS_CONSIDERED = "Feeding Supplements Considered"
        private const val FEEDING_CONSIDERED = "Feeding Considered"

        /**
         * Child Health Monitoring
         */
        private const val TEMP = "Baby's Temperature"
        private const val RESP_RATE = "Baby's Respiration Rate"
        private const val PULSE = "Baby's Pulse"
        private const val OXY = "Baby's Oxy Sat"
        private const val DISTRESS = "Respiration Distress"
        private const val APNOEA = "Apnoea"
        private const val BLOOD_SUGAR = "Blood Sugar"
        private const val VOLUME_EXPRESSED = "Volume Expressed"
        private const val VOLUME_GIVEN = "Volume Given"
        private const val NURSING_PLAN = "Nursing Plan"
        private const val IV_VOLUME = "IV volume given"
        private const val IV_LINE = "IV line Working"
        private const val DIAPERS_CHANGED = "Diapers Changed"
        private const val SOILED_DIAPER = "Soiled Diaper"
        private const val VOMITED = "Baby Vomited"
        private const val SHIFT_NOTES = "Shift Notes"

        /**
         * Discharge Details
         */
        private const val ASSESSMENT_DATE = "Date of Assessment"
        private const val FEED_ADJUSTMENT = "Feed Adjustment"
        private const val READY_FOR_DISCHARGE = "Ready for Discharge"
        private const val DATE_TODAY = "Today's Date"

        /**
         * APGAR Score
         */
        private const val APPEARANCE = "Appearance"
        private const val RECORD_TIME = "Time of Record"
        private const val ACTIVITY = "Activity"
        private const val GRIMACE = "Grimace"
        private const val PULSE_S = "Pulse"
        private const val RESPIRATION = "Respiration"
        private const val TOTAL_SCORE = "Apgar Score"

        const val SPO2 = "59408-5"

        val discharge_details = listOf(
            ASSESSMENT_DATE, FEED_ADJUSTMENT, ADDITIONAL_COMMENTS, READY_FOR_DISCHARGE, DATE_TODAY
        )
        val post_natal_milk_expression = listOf(
            AMOUNT_EXPRESSED, EXPRESSION_TIME, STORAGE_TIME, COMPLETED_BY
        )
        val post_natal_child_feeding = listOf(
            FEEDING_CUES, LATCHES, STEADY_SUCK, AUDIBLE_SWALLOW, NO_CHOCKING,
            BREAST_SOFTENING, TIME_PER_SIDE, COMFORTABLE_SLEEP, WET_DIAPER, MORE_WEIGHT_LOSS,
            EFFICIENT_BF, SUFFICIENT_BF

        )
        val post_natal_child_supplements = listOf(
            SUPPLEMENTS_CONSIDERED, FEEDING_CONSIDERED
        )
        val comorbidities: Set<String> =
            setOf(
                ASTHMA,
                LUNG_DISEASE,
                DEPRESSION,
                DIABETES,
                HYPER_TENSION,
                HEART_DISEASE,
                HIGH_BLOOD_LIPIDS
            )
        val symptoms: Set<String> = setOf(FEVER, SHORTNESS_BREATH, COUGH, LOSS_OF_SMELL)

        val assessment_unit_details = listOf(
            TEMP, RESP_RATE, PULSE, OXY, DISTRESS, BLOOD_SUGAR, VOLUME_EXPRESSED,
            VOLUME_GIVEN, NURSING_PLAN, IV_VOLUME, IV_LINE, DIAPERS_CHANGED,
            SOILED_DIAPER, VOMITED, SHIFT_NOTES
        )
        val custom_unit_details = listOf(
            ADDITIONAL_COMMENTS, APNOEA, SUFFICIENT_BF, BREATHING_DIFFICULTY, DISTRESS
        )
        val maternity_unit_details = listOf(
            "Labour Stage", "Vitamin K Given", "Is the Mother well",
            "Expected Date of Delivery", "Pregnancies,", "Parity", "Gravidity",
            "Last Menstrual Period", "Baby Status", "Placenta Complete",
            "Gestation", "HIV Status", "Total Babies", "Delivery Method",
            "Eye Prophylaxis", "Reason for CS", "Delivery Method,",
            "Breast Problems", "Mother's Health", "ROM",
        )
        val maternity_unit_child_details = listOf(
            APPEARANCE, RECORD_TIME, ACTIVITY, GRIMACE,
            PULSE, RESPIRATION, TOTAL_SCORE
        )
        val maternity_baby_registration =
            listOf(
                "Admitted From", "Temperature (Centigrade)",
                "Admission Reason", "Born Where",
                "Gestation", "Neonatal Sepsis",
                "Asphyxia", "Born Before Arrival,",
                "Born Before Arrival", "Low Birth Weight",
                "Assessment Date", COMPLETED_BY,
                "BirthWeight", "Baby is Well",
            )
        val newborn_unit_details = listOf(
            MOTHER_MEDICAL, ANTENATAL,
        )
        val child_newborn_unit_details = listOf(
            "Antibiotics Given",
            "Head Circumference",
            "Pulse Rate",
            "Baby has Fever",
            "Antibiotics Given",
            "Baby on KMC",
            "Care Given",
            "Temperature (Centigrade)",
            "Convulsions",
            "Time Baby Seen",
            "Resuscitated at Birth",
            "Breathing Difficulty",
            "Baby on Oxygen",
            "Respiration Rate (bpm)",
            "Oxygen Sat",
            "Apnoea",
            "CHX Used",
        )

        val child_feeding_needs = listOf(
            "Diagnosis",
            "Intervention",
        )
        val child_feed_prescription = listOf(
            "Difficulty Feeding",
            "Diagnosis",
            "Intervention",
            "Feeding volume required",
            "Total 24hr volume required",
            "Feeding Method",
            "Treatment Duration",
            "Prescribing Instructions",
            "Current weight",
            "Total input in 24hrs",
            "Volume of IV Fluids",
            "IV Fluids Required",
            "Prescription Time",
            "Feeding Frequency",
            "Baby's Day of Life",
            "Feeding Route",
        )
        val child_feeding_data = listOf(
            "Amount of feed offered to baby", "Any Feeding Remarks",
            "Amount of feed consumed by baby", "Soiled Diapers", "Wet Diapers",
            "Baby's current weight", "Time of Feeding",
        )
        val postnatal_unit_details = listOf(
            LACTATION_SUPPORT, CONTRAINDICATED, BM_THROUGH_BF, LACTATION_SUPPORT_TIME,
            COMPLETED_BY, PROPER_POSITIONING, EARLY_LICKING, MANUAL_EXPRESSION, AWAKENING_BABY,
            SUPPORT_SUCCESSFUL, EFFECTIVE_EXPRESSION, ADDITIONAL_COMMENTS, NIPPLE_STIMULATION
        )


        val human_milk_details = listOf(
            "Hospital Receiving DHM", "Recipient Gestation", "Recipient Location", "Consent Given,",
            "Consent Given", "Consent Date", "Clinician Designation", "Clinician Name",
            "Volume Prescribed per feed", "DHM Volume Ordered", "Legal Guardian Signature",
            "Name of Prescriber", "Dispensing Staff Name", "Nursing Staff Name",
            "Receiving Staff Name", "Preterm or Term Milk", "Expiry Date", "Hospital Receiving DHM",
            "Volume Dispensed", "Time of DHM Order", "Number of Bottles", "Order Accepted,",
            "Volume Dispensed", "Order Accepted", "Number of Bottles", "Total DHM Available",
            "Donor ID", "Reason for Receiving DHM", "Batch Number", "DHM Available,",
            "DHM Available", "DHM Volume Ordered",
        )


        val monitoring_unit_details = listOf(
            "Hospital Receiving DHM",
        )
    }
}