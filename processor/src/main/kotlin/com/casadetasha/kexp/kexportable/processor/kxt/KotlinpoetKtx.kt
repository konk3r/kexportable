package com.casadetasha.kexp.kexportable.processor.kxt

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassData

@OptIn(KotlinPoetMetadataPreview::class)
internal fun ClassData.isPropertyTransient(property: ImmutableKmProperty): Boolean {
    properties[property]?.allAnnotations
        ?.map { annotationSpec -> annotationSpec.typeName }

    return properties[property]?.allAnnotations
        ?.map { annotationSpec -> annotationSpec.typeName }
        ?.any { it == Transient::class.asTypeName() }
        ?: false
}

internal fun AnnotationSpec.getStringParameter(key: String): String? {
    return members.map {
        val splitMember = it.toString().split("=")
        Pair(splitMember[0].trim(), splitMember[1].trim())
    }
        .firstOrNull { it.first == key }
        ?.second
        ?.removeWrappingQuotes()
}
