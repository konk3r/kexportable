package com.casadetasha.kexp.kexportable.processor.kxt

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.casadetasha.kexp.kexportable.processor.KexportableClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmType

private val humps = "(?<=.)(?=\\p{Upper})".toRegex()

internal fun String.toSnakeCase() = replace(humps, "_").lowercase()

internal fun StringBuilder.removeTrailingComma() : StringBuilder {
    val index = lastIndexOf(",")
    if (index == lastIndex) {
        deleteCharAt(index)
    }
    return this
}

@OptIn(KotlinPoetMetadataPreview::class)
internal fun Set<KotlinContainer.KotlinClass>.containsMatchingType(type: KmType): Boolean {
    return map { KexportableClass(it) }
        .any { it.sourceClassName == type.toTypeName().asNonNullable() }
}

