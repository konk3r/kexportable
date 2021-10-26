package com.casadetasha.kexp.kexportable

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.casadetasha.kexp.kexportable.kexport.kexport
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test

class FunctionModelTest {

    val expectedJson = """
        {
           "requiredFunctionValue":"Junkyard Dog",
           "optionalFunctionValue":null,
           "nestedFunctionValue":{
              "stringValue":"The wrecking yard"
           }
        }
        """.trimIndent()

    @Test
    fun `kexported model is not null`() {
        val kexportedModel = FunctionValueModel(
            requiredModelValue = SingleValueModel("Legion of DOOM")
        ).kexport()
        assertThat(kexportedModel).isNotNull()
    }

    @Test
    fun `kexports functions with simple return types properly`() {
        val kexportedModel = FunctionValueModel(
            requiredModelValue = SingleValueModel("Legion of DOOM")
        ).kexport()
        assertThat(kexportedModel.requiredFunctionValue).isEqualTo("Boo ya")
    }

    @Test
    fun `kexports functions with Kexportable return types properly when null`() {
        val parentModel = FunctionValueModel(
            requiredModelValue = SingleValueModel("Legion of DOOM"),
            optionalValue = null
        )
        assertThat(parentModel.kexport().optionalFunctionValue).isNull()
    }

    @Test
    fun `kexports functions with Kexportable return types properly when present`() {
        val parentModel = FunctionValueModel(
            requiredModelValue = SingleValueModel("Legion of DOOM"),
            optionalValue = "Well howdy there, I'm optional"
        )
        assertThat(parentModel.kexport().optionalFunctionValue).isEqualTo("Well howdy there, I'm optional")
    }

    @Test
    fun `kexports functions with Kexportable return types properly`() {
        val kexportedParentModel = FunctionValueModel(
            requiredModelValue = SingleValueModel("Legion of DOOM")
        ).kexport()
        val nestedStringValue = kexportedParentModel.nestedFunctionValue.stringValue
        assertThat(nestedStringValue).isEqualTo("Legion of DOOM")
    }

    @Test
    @OptIn(ExperimentalSerializationApi::class)
    fun `parsed json from export matches expected json`() {
        val parsingModel = FunctionValueModel(
            requiredValue = "Junkyard Dog",
            optionalValue = null,
            requiredModelValue = SingleValueModel("The wrecking yard")
        ).kexport()

        val expectedJson = Json.decodeFromString<JsonElement>(expectedJson)
        val jsonValue = Json.encodeToJsonElement(parsingModel)
        assertThat(jsonValue).isEqualTo(expectedJson)
    }
}
