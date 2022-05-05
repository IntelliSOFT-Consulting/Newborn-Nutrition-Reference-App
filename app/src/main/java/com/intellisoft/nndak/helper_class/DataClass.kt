package com.intellisoft.nndak.helper_class

import org.hl7.fhir.r4.model.QuestionnaireResponse

enum class DbMotherKey {
    NATIONALID, FHIRID
}
data class DbMotherInfo(
    val nationalId:String,
    val motherDob:String,
    val firstName:String,
    val familyName:String,
    val phoneNumber:String,
    val fhirId:String,
)
data class DbQuestionnaireData(
    val linkId: String,
    val item: List<DbItem>,
)
data class DbItem(
    val linkId: String,
    val item: List<DbValueDate>
)
data class DbValueDate(
    val linkId: String,
    val answer: List<DbAnswer>
)
data class DbAnswer(
    val valueDate: String
)
data class DbFhirQuestion(
    val item: List<QuestionnaireResponse.QuestionnaireResponseItemComponent>
)