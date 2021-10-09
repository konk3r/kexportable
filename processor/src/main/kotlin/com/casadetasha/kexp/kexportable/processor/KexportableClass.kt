package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.kexportable.annotations.KexportName
import com.casadetasha.kexp.kexportable.annotations.Kexportable
import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.AS_WRITTEN
import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.SNAKE_CASE
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.ClassData

@OptIn(KotlinPoetMetadataPreview::class)
private fun ClassData.isPropertyTransient(property: ImmutableKmProperty): Boolean {
    properties[property]?.allAnnotations
        ?.map { annotationSpec -> annotationSpec.typeName }

    return properties[property]?.allAnnotations
        ?.map { annotationSpec -> annotationSpec.typeName }
        ?.any { it == Transient::class.asTypeName() }
        ?: false
}

private val humps = "(?<=.)(?=\\p{Upper})".toRegex()
fun String.toSnakeCase() = replace(humps, "_").lowercase()

@OptIn(KotlinPoetMetadataPreview::class)
internal class KexportableClass(
    private val classData: ClassData,
    kexportableAnnotation: Kexportable? = null
) {
    companion object {
        internal const val EXPORT_METHOD_SIMPLE_NAME = "kexport"
        private const val EXPORTABLE_PACKAGE_SEGMENT = "kexport"
        private const val EXPORTABLE_CLASS_PREFIX = "Kexported"
    }

    val packageName: String by lazy {
        val sourcePackageName: String = classData.className.packageName
        val packagePrefix = if (sourcePackageName.isNotBlank()) "$sourcePackageName." else ""
        packagePrefix + EXPORTABLE_PACKAGE_SEGMENT
    }

    val sourceClassName: ClassName = classData.className
    private val sourceClassSimpleName: String = sourceClassName.simpleName
    val className = ClassName(
        packageName = packageName,
        simpleNames = listOf(EXPORTABLE_CLASS_PREFIX + sourceClassSimpleName)
    )
    val classSimpleName: String = className.simpleName

    private val namingConvention: Kexportable.NamingConvention? = kexportableAnnotation?.namingConvention

    private val defaultSerialName: String? = when(kexportableAnnotation?.namingConvention) {
        AS_WRITTEN -> sourceClassSimpleName
        SNAKE_CASE -> sourceClassSimpleName.toSnakeCase()
        else -> null
    }
    val serialName : String? = when(kexportableAnnotation?.exportName?.isNotBlank()) {
            true -> kexportableAnnotation.exportName
            false ->  defaultSerialName
            else -> null
    }

    val exportableProperties: Sequence<ImmutableKmProperty> = classData.properties
            .asSequence()
            .map { it.key }
            .filter { it.isPublic }
            .filter { it.isDeclaration }
            .filterNot { it.isSynthesized }
            .filterNot { classData.isPropertyTransient(it) }

    internal fun ImmutableKmProperty.getSerialName(): String {
        return classData.properties[this]
            ?.allAnnotations
            ?.firstOrNull { it.typeName == KexportName::class.asTypeName() }
            ?.getStringParameter("value")
            ?: getDefaultSerialName()
    }

    private fun ImmutableKmProperty.getDefaultSerialName(): String {
        return when(namingConvention) {
            AS_WRITTEN -> name
            SNAKE_CASE -> name.toSnakeCase()
            else -> throw IllegalStateException(
                "ExportableClass must be initialized with Exportable annotation to get SerialName")
        }
    }
}

private fun AnnotationSpec.getStringParameter(key: String): String? {
    return members.map {
        val splitMember = it.toString().split("=")
        Pair(splitMember[0].trim(), splitMember[1].trim())
    }
        .firstOrNull { it.first == key }
        ?.second
        ?.removeWrappingQuotes()
}

private fun String.removeWrappingQuotes(): String = removePrefix("\"").removeSuffix("\"")
