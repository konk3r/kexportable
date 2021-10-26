package com.casadetasha.kexp.kexportable

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.casadetasha.kexp.kexportable.kexport.KexportedSingleValueModel
import com.casadetasha.kexp.kexportable.kexport.kexport
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test

class NestedModelTest {
    companion object {
        val expectedJson = """ {
                "export_named_value": "This name should be snake_case",
                "nestedMultiValueModel": {
                    "booleanValue": true,
                    "intValue": 24,
                    "optionalValue": null,
                    "stringValue": "My model should be camelCase"
                },
                "firstOptionalNestedModel": {
                    "stringValue": "This model shouldn't be null"
                },
                "secondOptionalNestedModel": null,
                "renamed_nested_model": {
                    "stringValue": "My model name should be snake_case"
                },
                "standardValue": "Mah val!",
                "wrongAnnotationValue": "This name should be camelCase"
            }
        """.trimIndent()
    }

    @Test
    fun `kexports a nested exportable model as the exported version`() {
        val parentModel = ParentModel(SingleValueModel("Hulkamania 97, BRUTHA!"))
        assertThat(parentModel.kexport().nestedModel).isInstanceOf(KexportedSingleValueModel::class)
    }

    @Test
    fun `kexports nested fields properly`() {
        val parentModel = ParentModel(SingleValueModel("Hulkamania 98, BRUTHA!"))
        val nestedStringValue = parentModel.kexport().nestedModel.stringValue
        assertThat(nestedStringValue).isEqualTo("Hulkamania 98, BRUTHA!")
    }

    @Test
    @OptIn(ExperimentalSerializationApi::class)
    fun `parsed json from export matches expected json`() {
        val parsingModel = ComplexParsingModel().apply {
            standardValue = "Mah val!"
            transientValue = "This shouldn't show up"
            kexportNamedValue = "This name should be snake_case"
            wrongAnnotationValue = "This name should be camelCase"
            renamedNestedModel = SingleValueModel("My model name should be snake_case")
            nestedMultiValueModel = MultiValueModel(
                stringValue = "My model should be camelCase",
                booleanValue = true,
                intValue = 24,
                optionalValue = null
            )
            firstOptionalNestedModel = SingleValueModel("This model shouldn't be null")
            secondOptionalNestedModel = null
        }.kexport()

        val expectedJson = Json.decodeFromString<JsonElement>(expectedJson)
        val jsonValue = Json.encodeToJsonElement(parsingModel)
        assertThat(jsonValue).isEqualTo(expectedJson)
    }
}