package com.casadetasha.kexp.kexportable

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.casadetasha.kexp.kexportable.kexport.kexport
import org.junit.Test

class FunctionModelTest {

    @Test
    fun `kexported model is not null`() {
        val kexportedModel = FunctionValueModel(
            requiredValueModel = SingleValueModel("Legion of DOOM")
        ).kexport()
        assertThat(kexportedModel).isNotNull()
    }

    @Test
    fun `kexports functions with simple return types properly`() {
        val kexportedModel = FunctionValueModel(
            requiredValueModel = SingleValueModel("Legion of DOOM")
        ).kexport()
        assertThat(kexportedModel.requiredFunctionValue).isEqualTo("Boo ya")
    }

    @Test
    fun `kexports functions with Kexportable return types properly when null`() {
        val parentModel = FunctionValueModel(
            requiredValueModel = SingleValueModel("Legion of DOOM"),
            optionalValue = null
        )
        assertThat(parentModel.kexport().optionalFunctionValue).isNull()
    }

    @Test
    fun `kexports functions with Kexportable return types properly when present`() {
        val parentModel = FunctionValueModel(
            requiredValueModel = SingleValueModel("Legion of DOOM"),
            optionalValue = "Well howdy there, I'm optional"
        )
        assertThat(parentModel.kexport().optionalFunctionValue).isEqualTo("Well howdy there, I'm optional")
    }

    @Test
    fun `kexports functions with Kexportable return types properly`() {
        val kexportedParentModel = FunctionValueModel(
            requiredValueModel = SingleValueModel("Legion of DOOM")
        ).kexport()
        val nestedStringValue = kexportedParentModel.nestedFunctionValue.stringValue
        assertThat(nestedStringValue).isEqualTo("Legion of DOOM")
    }
}
