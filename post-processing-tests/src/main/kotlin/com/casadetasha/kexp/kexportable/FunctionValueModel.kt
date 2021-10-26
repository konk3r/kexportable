package com.casadetasha.kexp.kexportable

import com.casadetasha.kexp.kexportable.annotations.Kexportable

@Kexportable
class FunctionValueModel(
    private val requiredModelValue: SingleValueModel,
    private val requiredValue: String = "Boo ya",
    private val optionalValue: String? = ""
    ) {
    @Kexportable
    fun requiredFunctionValue(): String = requiredValue

    @Kexportable
    fun optionalFunctionValue(): String? = optionalValue

    @Kexportable
    fun nestedFunctionValue(): SingleValueModel = requiredModelValue
}
