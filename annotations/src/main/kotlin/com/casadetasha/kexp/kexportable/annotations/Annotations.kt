package com.casadetasha.kexp.kexportable.annotations

import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.AS_WRITTEN

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Kexportable(val exportName: String = "", val namingConvention: NamingConvention = AS_WRITTEN) {
    enum class NamingConvention {
        AS_WRITTEN,
        SNAKE_CASE
    }
}

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class KexportName(val value: String)
