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

        /***
         * APGAR Score
         *
         * ***/
        const val AP00 = "AP/00"
        const val AP01 = "AP/01"
        const val AP02 = "AP/02"
        const val HR00 = "HR/00"
        const val HR01 = "HR/01"
        const val HR02 = "HR/02"
        const val GM00 = "GM/00"
        const val GM01 = "GM/01"
        const val GM02 = "GM/02"
        const val A00 = "A/00"
        const val A01 = "A/01"
        const val A02 = "A/02"
        const val R00 = "R/00"
        const val R01 = "R/01"
        const val R02 = "R/02"

        val total_ap: Set<String> =
            setOf(AP00, AP01, AP02)

        val total_hr: Set<String> =
            setOf(HR00, HR01, HR02)

        val total_gm: Set<String> =
            setOf(GM00, GM01, GM02)

        val total_act: Set<String> =
            setOf(A00, A01, A02)

        val total_resp: Set<String> =
            setOf(R00, R01, R02)

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