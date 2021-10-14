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
}
