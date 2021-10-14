package com.casadetasha.kexp.kexportable

import com.casadetasha.kexp.kexportable.annotations.Kexportable

@Kexportable
class FunctionValueModel(
    private val requiredValueModel: SingleValueModel,
    private val optionalValue: String? = ""
    ) {
    @Kexportable
    fun requiredFunctionValue(): String = "Boo ya"

    @Kexportable
    fun optionalFunctionValue(): String? = optionalValue

    @Kexportable
    fun nestedFunctionValue(): SingleValueModel = requiredValueModel
}
