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
         * Roles Access
         */
        const val ADMIN = "ADMINISTRATOR"
        const val HMB = "HMB Assistant"
        const val DOCTOR = "Doctor"
        const val NURSE = "Nurse"

        /**
         * Prescriptions
         */
        const val PRESCRIPTION = "Feeds Prescription"
        const val BABY_ASSESSMENT = "Baby Assessment"
        const val DHM_STOCK = "DHM Stock"
        const val FEEDING_MONITORING = "Feeding and Monitoring"
        const val ADMISSION_WEIGHT = "29463-7"
        const val CURRENT_WEIGHT = "3141-9"
        const val EBM = "226790003"
        const val FEEDS_TAKEN = "Total-Taken"
        const val DIAPER_CHANGED = "Diaper-Changed"
        const val DHM_VOLUME = "DHM-Volume"
        const val IV_VOLUME = "IV-Volume"
        const val EBM_VOLUME = "EBM-Volume"
        const val STOOL = "Stool"
        const val ADJUST_PRESCRIPTION = "Adjust-Prescription"
        const val FEEDS_DEFICIT = "Feeds-Deficit"
        const val VOMIT = "Vomit"
        const val REMARKS = "Additional-Notes"
        const val EXPRESSED_MILK = "226790003"
        const val EXPRESSION_TIME = "Time-Expressed"

        /**
         * 104-9 Code
         * 2hr 3hr 2hr 3hr 1hr 2hr value
         * 3 2 1 count
         */


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


    }
}