package com.intellisoft.nndak.logic

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
            "Labour Stage", "Expected Date of Delivery", "Last Menstrual Period", "Gestation",
        )
        val maternity_unit_child_details = listOf(
            "Appearance", "Time of Record", "Activity", "Grimace", "Pulse", "Respiration",
        )
        val newborn_unit_details = listOf(
            "Hospital Receiving DHM",
        )
        val postnatal_unit_details = listOf(
            "Hospital Receiving DHM",
        )
        val custom_unit_details = listOf(
            "Hospital Receiving DHM",
        )
        val human_milk_details = listOf(
            "Hospital Receiving DHM",
            "Recipient Gestation",
            "Recipient Location",
            "Consent Given,",
            "Consent Given",
            "Consent Date",
            "Clinician Designation",
            "Clinician Name",
            "Volume Prescribed per feed",
            "DHM Volume Ordered",
            "Legal Guardian Signature",
            "Name of Prescriber", "Dispensing Staff Name",
            "Nursing Staff Name", "Receiving Staff Name",
            "Preterm or Term Milk", "Expiry Date",
            "Hospital Receiving DHM", "Volume Dispensed",
            "Time of DHM Order", "Number of Bottles",
            "Order Accepted,", "Volume Dispensed",
            "Order Accepted", "Number of Bottles",
            "Total DHM Available", "Donor ID",
            "Reason for Receiving DHM", "Batch Number",
            "DHM Available,", "DHM Available", "DHM Volume Ordered",
        )
        val assessment_unit_details = listOf(
            "Hospital Receiving DHM",
        )

        val monitoring_unit_details = listOf(
            "Hospital Receiving DHM",
        )
    }
}