package com.casadetasha.kexp.kexportable

import com.casadetasha.kexp.kexportable.annotations.Kexportable

@Kexportable
data class ParentModel(val nestedModel: SingleValueModel)