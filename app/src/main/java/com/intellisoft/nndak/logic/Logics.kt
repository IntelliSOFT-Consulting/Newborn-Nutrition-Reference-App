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
    }
}