package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.casadetasha.kexp.annotationparser.KotlinFunction
import com.casadetasha.kexp.kexportable.annotations.KexportName
import com.casadetasha.kexp.kexportable.annotations.Kexportable
import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.AS_WRITTEN
import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.SNAKE_CASE
import com.casadetasha.kexp.kexportable.processor.kxt.getStringParameter
import com.casadetasha.kexp.kexportable.processor.kxt.isPropertyTransient
import com.casadetasha.kexp.kexportable.processor.kxt.toSnakeCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
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
    private val namingConvention: Kexportable.NamingConvention? = kexportableAnnotation?.namingConvention

    private val defaultSerialName: String? = when (kexportableAnnotation?.namingConvention) {
        AS_WRITTEN -> sourceClassSimpleName
        SNAKE_CASE -> sourceClassSimpleName.toSnakeCase()
        else -> null
    }
    val serialName: String? = when (kexportableAnnotation?.exportName?.isNotBlank()) {
        true -> kexportableAnnotation.exportName
        false -> defaultSerialName
        else -> null
    }

    val exportableProperties: Set<ImmutableKmProperty> = sourceClass.properties
        .asSequence()
        .map { it.key }
        .filter { it.isPublic }
        .filter { it.isDeclaration }
        .filterNot { it.isSynthesized }
        .filterNot { sourceClassData.isPropertyTransient(it) }
        .toSet()

    val exportableFunctions: Set<KotlinFunction> = sourceClass.getFunctionsAnnotatedWith(Kexportable::class)

    // TODO: Create KotlinProperty to group ImmutableKmProperties and Elements to stop using this hack
    internal fun ImmutableKmProperty.getSerialName(): String {
        return sourceClassData.properties[this]
            ?.allAnnotations
            ?.firstOrNull { it.typeName == KexportName::class.asTypeName() }
            ?.getStringParameter("value")
            ?: getDefaultSerialName(this.name)
    }

    internal fun KotlinFunction.getSerialName(): String = (getAnnotation(Kexportable::class) as Kexportable).exportName
        .ifBlank { getDefaultSerialName(simpleName) }

    private fun getDefaultSerialName(name: String): String {
        return when (namingConvention) {
            AS_WRITTEN -> name
            SNAKE_CASE -> name.toSnakeCase()
            else -> throw IllegalStateException(
                "ExportableClass must be initialized with Exportable annotation to get SerialName"
            )
        }
    }
}
