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

        /**
         * Lactation Assessment
         */
        private const val LACTATION_SUPPORT_TIME = "Time of Lactation Support"
        private const val PROPER_POSITIONING = "Proper Positioning"
        private const val EARLY_LICKING = "Early Licking"
        private const val MANUAL_EXPRESSION = "Manual Expression"
        private const val AWAKENING_BABY = "Awakening Baby"
        private const val SUPPORT_SUCCESSFUL = "Lactation Support Successful"
        private const val EFFECTIVE_EXPRESSION = "Effectively Expressing Milk"
        private const val ADDITIONAL_COMMENTS = "Additional comments"

        /**
         * Milk Expression
         */

        private const val AMOUNT_EXPRESSED = "Amount Expressed"
        private const val EXPRESSION_TIME = "Time of Expression"
        private const val STORAGE_TIME = "Time of Storage"

        const val SPO2 = "59408-5"

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

        val maternity_unit_details = listOf(
            "Labour Stage", "Vitamin K Given", "Is the Mother well",
            "Expected Date of Delivery", "Pregnancies,", "Parity", "Gravidity",
            "Last Menstrual Period", "Baby Status", "Placenta Complete",
            "Gestation", "HIV Status", "Total Babies", "Delivery Method",
            "Eye Prophylaxis", "Reason for CS", "Delivery Method,",
            "Breast Problems", "Mother's Health", "ROM",
        )
        val maternity_unit_child_details = listOf(
            "Appearance",
            "Time of Record",
            "Activity",
            "Grimace",
            "Pulse",
            "Respiration",
            "Apgar Score",
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
            "Mother's Medical Condition", "Antenatal Corticosteroids"
        )
        val child_newborn_unit_details = listOf(
            "Antibiotics Given",
            "Head Circumference",
            "Pulse Rate",
            "Baby has Fever",
            "Difficulty Feeding",
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
            "Difficulty Feeding",
            "Diagnosis",
            "Intervention",
            COMPLETED_BY,
            "Respiratory Distress",
        )
        val child_feed_prescription = listOf(
            "Difficulty Feeding",
            "Diagnosis",
            "Intervention",
            COMPLETED_BY,
            "Respiratory Distress",
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
            SUPPORT_SUCCESSFUL, EFFECTIVE_EXPRESSION, ADDITIONAL_COMMENTS
        )
        val post_natal_milk_expression = listOf(
            AMOUNT_EXPRESSED, EXPRESSION_TIME, STORAGE_TIME, COMPLETED_BY
        )
        val custom_unit_details = listOf(
            "Hospital Receiving DHM",
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
        val assessment_unit_details = listOf(
            "Hospital Receiving DHM",
        )

        val monitoring_unit_details = listOf(
            "Hospital Receiving DHM",
        )
    }
}