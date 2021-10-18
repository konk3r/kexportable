package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.casadetasha.kexp.annotationparser.KotlinValue.KotlinFunction
import com.casadetasha.kexp.annotationparser.KotlinValue.KotlinProperty
import com.casadetasha.kexp.kexportable.annotations.KexportName
import com.casadetasha.kexp.kexportable.annotations.Kexportable
import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.AS_WRITTEN
import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.SNAKE_CASE
import com.casadetasha.kexp.kexportable.processor.kxt.isPropertyTransient
import com.casadetasha.kexp.kexportable.processor.kxt.toSnakeCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.metadata.*

@OptIn(KotlinPoetMetadataPreview::class)
internal class KexportableClass(
    private val sourceClass: KotlinContainer.KotlinClass,
    kexportableAnnotation: Kexportable? = null
) {
    companion object {
        internal const val EXPORT_METHOD_SIMPLE_NAME = "kexport"
        private const val EXPORTABLE_PACKAGE_SEGMENT = "kexport"
        private const val EXPORTABLE_CLASS_PREFIX = "Kexported"
    }

    private val sourceClassData = sourceClass.classData

    val packageName: String by lazy {
        val sourcePackageName: String = sourceClass.packageName
        val packagePrefix = if (sourcePackageName.isNotBlank()) "$sourcePackageName." else ""
        packagePrefix + EXPORTABLE_PACKAGE_SEGMENT
    }

    val sourceClassName: ClassName = sourceClass.className
    private val sourceClassSimpleName: String = sourceClass.classSimpleName

    val className = ClassName(
        packageName = packageName,
        simpleNames = listOf(EXPORTABLE_CLASS_PREFIX + sourceClassSimpleName)
    )
    val classSimpleName: String = className.simpleName
    val namingConvention: Kexportable.NamingConvention = kexportableAnnotation?.namingConvention ?: AS_WRITTEN

    private val defaultSerialName: String = when (namingConvention) {
        AS_WRITTEN -> sourceClassSimpleName
        SNAKE_CASE -> sourceClassSimpleName.toSnakeCase()
    }
    val serialName: String? = when (kexportableAnnotation?.exportName?.isNotBlank()) {
        true -> kexportableAnnotation.exportName
        false -> defaultSerialName
        else -> null
    }

    val kexportableProperties: Set<KotlinProperty> = sourceClass.kotlinProperties
        .asSequence()
        .filter { it.property.isPublic }
        .filter { it.property.isDeclaration }
        .filterNot { it.property.isSynthesized }
        .filterNot { sourceClassData.isPropertyTransient(it.property) }
        .toSet()

    val kexportableFunctions: Set<KotlinFunction> = sourceClass.getFunctionsAnnotatedWith(Kexportable::class)
}
