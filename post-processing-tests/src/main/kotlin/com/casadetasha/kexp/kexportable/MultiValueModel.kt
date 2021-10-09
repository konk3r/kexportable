package com.casadetasha.kexp.kexportable

import com.casadetasha.kexp.kexportable.annotations.Kexportable

@Kexportable
data class MultiValueModel(val stringValue: String,
                           val booleanValue: Boolean,
                           val intValue: Int,
                           val optionalValue: String?)