package com.casadetasha.kexp.kexportable

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.casadetasha.kexp.kexportable.kexport.kexport
import org.junit.Test

class SingleModelTest {
    @Test
    fun `exported model is not null`() {
        val exportedModel = MultiValueModel("", true, 42, null).kexport()
        assertThat(exportedModel).isNotNull()
    }

    @Test
    fun `exported values match the original values`() {
        val exportedModel = MultiValueModel(
            stringValue = "testing testables",
            booleanValue = true,
            intValue = 37,
            optionalValue = null
        ).kexport()

        assertThat(exportedModel.stringValue).isEqualTo("testing testables")
        assertThat(exportedModel.booleanValue).isEqualTo(true)
        assertThat(exportedModel.intValue).isEqualTo(37)
        assertThat(exportedModel.optionalValue).isNull()
    }
}
