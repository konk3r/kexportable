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

class SingleModelTest {

    companion object {
        val expectedJson = """
            {
                "stringValue":"testing testables",
                "optionalValue":null,
                "intValue":37,
                "booleanValue":true
            }
            """.trimIndent()
    }

    @Test
    fun `exported model is not null`() {
        val kexportedModel = MultiValueModel("", true, 42, null).kexport()
        assertThat(kexportedModel).isNotNull()
    }

    @Test
    fun `exported values match the original values`() {
        val kexportedModel = MultiValueModel(
            stringValue = "testing testables",
            booleanValue = true,
            intValue = 37,
            optionalValue = null
        ).kexport()

        assertThat(kexportedModel.stringValue).isEqualTo("testing testables")
        assertThat(kexportedModel.booleanValue).isEqualTo(true)
        assertThat(kexportedModel.intValue).isEqualTo(37)
        assertThat(kexportedModel.optionalValue).isNull()
    }

    @Test
    @OptIn(ExperimentalSerializationApi::class)
    fun `parsed json from export matches expected json`() {
        val parsingModel = MultiValueModel(
            stringValue = "testing testables",
            booleanValue = true,
            intValue = 37,
            optionalValue = null
        ).kexport()

        val expectedJson = Json.decodeFromString<JsonElement>(expectedJson)
        val jsonValue = Json.encodeToJsonElement(parsingModel)
        assertThat(jsonValue).isEqualTo(expectedJson)
    }
}
