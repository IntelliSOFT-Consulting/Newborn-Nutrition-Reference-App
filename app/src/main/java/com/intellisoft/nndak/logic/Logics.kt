package com.intellisoft.nndak.logic

import org.hl7.fhir.r4.model.codesystems.ListOrder

class Logics {

    companion object {
        const val ASTHMA = "161527007"
        const val LUNG_DISEASE = "13645005"
        const val DEPRESSION = "35489007"
        const val DIABETES = "161445009"
        const val HYPER_TENSION = "161501007"
        const val HEART_DISEASE = "56265001"
        const val HIGH_BLOOD_LIPIDS = "161450003"
        const val FEVER = "386661006"
        const val SHORTNESS_BREATH = "13645005"
        const val COUGH = "49727002"
        const val LOSS_OF_SMELL = "44169009"

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
            "Expected Date of Delivery", "Pregnancies,","Parity","Gravidity",
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
                "Assessment Date", "Completed By",
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
            "Completed By",
            "Respiratory Distress",
        )
        val child_feed_prescription = listOf(
            "Difficulty Feeding",
            "Diagnosis",
            "Intervention",
            "Completed By",
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
            "Difficulty Feeding",
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