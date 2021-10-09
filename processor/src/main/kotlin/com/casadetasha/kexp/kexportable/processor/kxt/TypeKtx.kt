package com.casadetasha.kexp.kexportable.processor.kxt

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.specs.TypeNameAliasTag
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.KmClassifier

@KotlinPoetMetadataPreview
internal fun ImmutableKmType.toTypeName(): TypeName {
    val type: TypeName = when (val valClassifier = classifier) {
        is KmClassifier.Class -> {
            ClassInspectorUtil.createClassName(valClassifier.name)
        }
        else -> throw IllegalArgumentException("Only class classifiers are currently supported.")
    }
    val finalType = type.copy(nullable = isNullable)

    return abbreviatedType?.let {
        // This is actually an alias! The "abbreviated type" is the alias and how it's actually
        // represented in source. So instead - we'll return the abbreviated type but store the "real"
        // type in tags for reference.
        val abbreviatedTypeName = it.toTypeName()
        abbreviatedTypeName.copy(
            tags = mapOf(TypeNameAliasTag::class to TypeNameAliasTag(finalType))
        )
    } ?: finalType
}

internal fun TypeName.asNonNullable(): TypeName {
    return copy(nullable = false)
}
