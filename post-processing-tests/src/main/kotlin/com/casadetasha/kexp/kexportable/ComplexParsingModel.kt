package com.casadetasha.kexp.kexportable

import com.casadetasha.kexp.kexportable.annotations.KexportName
import com.casadetasha.kexp.kexportable.annotations.Kexportable
import kotlinx.serialization.SerialName

@Kexportable("complex_parsing_model")
class ComplexParsingModel {

    var standardValue: String? = null

    @Transient var transientValue: String? = null

    @KexportName("export_named_value")
    var kexportNamedValue: String? = null

    @SerialName("wrong_annotation_value")
    var wrongAnnotationValue: String? = null

    @KexportName("renamed_nested_model")
    lateinit var renamedNestedModel: SingleValueModel

    lateinit var nestedMultiValueModel: MultiValueModel

    var firstOptionalNestedModel: SingleValueModel? = null

    var secondOptionalNestedModel: SingleValueModel? = null

}