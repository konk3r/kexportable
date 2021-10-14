package com.casadetasha.kexp.kexportable

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.casadetasha.kexp.kexportable.kexport.kexport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test

class SnakeCaseModelTest {
    companion object {
        val expectedJson = """ 
            {
              "changed_name":"This should be \"changed_name\"",
              "hiddenAnnotationTestVal":"This should not be annotated",
              "test_val":"This should be snake case"
            }
            """.trimIndent()
    }

    @Test
    fun `kexported model is not null`() {
        val snakeCaseModel = SnakeCaseModel().kexport()
        assertThat(snakeCaseModel).isNotNull()
    }

    @Test
    fun `kexported values match the original values`() {
        val parsingModel = SnakeCaseModel(
            testVal = "This should be snake case",
            annotatedTestVal = "This should be \"changed_name\"",
            hiddenAnnotationTestVal = "This should not be annotated"
        ).kexport()

        val expectedJson = Json.decodeFromString<JsonElement>(expectedJson)
        val jsonValue = Json.encodeToJsonElement(parsingModel)
        assertThat(jsonValue).isEqualTo(expectedJson)
    }
}