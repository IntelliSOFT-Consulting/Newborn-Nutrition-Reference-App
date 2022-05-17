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

        /**
         * Child Details:: New Born
         */
        private const val ANTI_GIVEN = "Antibiotics Given"
        private const val HEAD_CIRCUMFERENCE = "Head Circumference"
        private const val PULSE_RATE = "Pulse Rate"
        private const val HAS_FEVER = "Baby has Fever"
        private const val KMC = "Baby on KMC"
        private const val CARE = "Care Given"
        private const val TEMP_C = "Temperature (Centigrade)"
        private const val CONVULSIONS = "Convulsions"
        private const val TIME_SEEN = "Time Baby Seen"
        private const val RESUSCITATED = "Resuscitated at Birth"
        private const val BREATHING_DIFF = "Breathing Difficulty"
        private const val ON_OXYGEN = "Baby on Oxygen"
        private const val RES_RATE = "Respiration Rate (bpm)"
        private const val OXY_SATE = "Oxygen Sat"
        private const val CHX = "CHX Used"

        /**
         * Maternity Unit Details
         */

        private const val LABOR_STAGE = "Labour Stage"
        private const val VITAMIN_K = "Vitamin K Given"
        private const val MOTHER_WELL = "Is the Mother well"
        private const val EDD = "Expected Date of Delivery"
        private const val PREGNANCIES = "Pregnancies,"
        private const val PARITY = "Parity"
        private const val GRAVIDITY = "Gravidity"
        private const val LMP = "Last Menstrual Period"
        private const val BABY_STATUS = "Baby Status"
        private const val COMPLETE_PLACENTA = "Placenta Complete"
        private const val GESTATION = "Gestation"
        private const val HIV_STATUS = "HIV Status"
        private const val TOTAL_BABIES = "Total Babies"
        private const val DELIVERY_METHOD = "Delivery Method"
        private const val EYE_PROPHYLAXIS = "Eye Prophylaxis"
        private const val CS_REASON = "Reason for CS"
        private const val CS_DELIVERY = "Delivery Method,"
        private const val BREAST_PROBLEMS = "Breast Problems"
        private const val MOTHER_STATUS = "Mother's Health"
        private const val ROM = "ROM"


        /**
         * Maternity Baby's Registration Details
         */
        private const val ADMITTED_FROM = "Admitted From"
        private const val ADMISSION_REASON = "Admission Reason"
        private const val BORN_WHERE = "Born Where"
        private const val NEONATAL_SEPSIS = "Neonatal Sepsis"
        private const val ASPHYXIA = "Asphyxia"
        private const val BBA = "Born Before Arrival,"
        private const val BBA_A = "Born Before Arrival"
        private const val LBW = "Low Birth Weight"
        private const val BIRTH_WEIGHT = "BirthWeight"
        private const val BABY_WELL = "Baby is Well"

        val maternity_baby_registration =
            listOf(
                TEMP_C, ASSESSMENT_DATE, ADMISSION_REASON, BORN_WHERE, NEONATAL_SEPSIS,
                GESTATION, ASPHYXIA, BBA, BBA_A, LBW, BIRTH_WEIGHT, BIRTH_WEIGHT,
                ADMITTED_FROM, BABY_WELL, COMPLETED_BY,
            )

        const val SPO2 = "59408-5"

        val maternity_unit_details = listOf(
            LABOR_STAGE, VITAMIN_K, MOTHER_WELL, EDD, PREGNANCIES,
            PARITY, GRAVIDITY, LMP, BABY_STATUS, COMPLETE_PLACENTA,
            GESTATION, HIV_STATUS, TOTAL_BABIES, DELIVERY_METHOD,
            EYE_PROPHYLAXIS, CS_REASON, CS_DELIVERY, BREAST_PROBLEMS,
            MOTHER_STATUS, ROM
        )
        val child_newborn_unit_details = listOf(
            ANTI_GIVEN, HEAD_CIRCUMFERENCE, PULSE_RATE, HAS_FEVER, KMC, CARE, TEMP_C, CONVULSIONS,
            TIME_SEEN, RESUSCITATED, BREATHING_DIFF, ON_OXYGEN, RES_RATE,
            OXY_SATE, APNOEA, CHX,

            )
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

        val maternity_unit_child_details = listOf(
            APPEARANCE, RECORD_TIME, ACTIVITY, GRIMACE,
            PULSE_S, RESPIRATION, TOTAL_SCORE
        )

        val newborn_unit_details = listOf(
            MOTHER_MEDICAL, ANTENATAL,
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