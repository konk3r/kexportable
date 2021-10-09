package com.casadetasha.kexp.kexportable;


import com.casadetasha.kexp.kexportable.annotations.KexportName
import com.casadetasha.kexp.kexportable.annotations.Kexportable;
import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.SNAKE_CASE

@Kexportable(namingConvention = SNAKE_CASE)
data class SnakeCaseModel (
    val testVal: String = "",
    @KexportName("changed_name") val annotatedTestVal: String? = null,
    @KexportName("hiddenAnnotationTestVal") val hiddenAnnotationTestVal: String? = null
)
