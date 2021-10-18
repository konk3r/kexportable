package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.casadetasha.kexp.annotationparser.KotlinValue
import com.casadetasha.kexp.annotationparser.KotlinValue.KotlinFunction
import com.casadetasha.kexp.annotationparser.KotlinValue.KotlinProperty
import com.casadetasha.kexp.kexportable.annotations.KexportName
import com.casadetasha.kexp.kexportable.annotations.Kexportable
import com.casadetasha.kexp.kexportable.processor.kxt.asNonNullable
import com.casadetasha.kexp.kexportable.processor.kxt.toSnakeCase
import com.casadetasha.kexp.kexportable.processor.kxt.toTypeName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(KotlinPoetMetadataPreview::class)
class KexportedClassSpecBuilder(private val exportableClasses: Set<KotlinContainer.KotlinClass>) {

    internal fun getClassSpec(kexportableClass: KexportableClass): TypeSpec {
        val classTypeBuilder = TypeSpec.classBuilder(kexportableClass.className)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class)
            .addAnnotation(
                AnnotationSpec.builder(SerialName::class)
                    .addMember("%S", kexportableClass.serialName!!)
                    .build()
            )

        val kotlinValues: List<KotlinValue> = sortedSetOf(
            *kexportableClass.kexportableFunctions.toTypedArray(),
            *kexportableClass.kexportableProperties.toTypedArray()
        ).reversed()

        kotlinValues.forEach {
            classTypeBuilder.addProperty(getKexportedPropertySpec(kexportableClass.namingConvention, it))
        }

        return classTypeBuilder
            .primaryConstructor(createConstructorSpec(kotlinValues))
            .build()
    }

    private fun getKexportedPropertySpec(namingConvention: Kexportable.NamingConvention,
                                         kotlinValue: KotlinValue): PropertySpec {
        return when (kotlinValue) {
            is KotlinProperty -> getKexportedPropertySpec(namingConvention, kotlinValue)
            is KotlinFunction -> getKexportedFunctionSpec(namingConvention, kotlinValue)
        }
    }

    private fun getKexportedPropertySpec(namingConvention: Kexportable.NamingConvention, property: KotlinProperty): PropertySpec {
        val propertyTypeName = property.property.returnType.toTypeName()
        val propertyBuilder = PropertySpec.builder(property.property.name, mapPropertyTypeName(propertyTypeName))
        val serialName = property.getSerialName(namingConvention)

        if (serialName != property.property.name) {
            propertyBuilder.addAnnotation(
                AnnotationSpec.builder(SerialName::class)
                    .addMember("%S", serialName)
                    .build()
            )
        }

        return propertyBuilder
            .initializer(property.property.name)
            .build()
    }

    private fun getKexportedFunctionSpec(
        namingConvention: Kexportable.NamingConvention,
        function: KotlinFunction
    ): PropertySpec {
        val propertyTypeName = function.returnType.toTypeName()
        val propertyBuilder = PropertySpec.builder(function.simpleName, mapPropertyTypeName(propertyTypeName))
        val serialName = function.getSerialName(namingConvention)

        if (serialName != function.simpleName) {
            propertyBuilder.addAnnotation(
                AnnotationSpec.builder(SerialName::class)
                    .addMember("%S", serialName)
                    .build()
            )
        }

        return propertyBuilder
            .initializer(function.simpleName)
            .build()
    }

    private fun createConstructorSpec(kotlinValues: Iterable<KotlinValue>): FunSpec {
        val constructorBuilder = FunSpec.constructorBuilder()
        kotlinValues.forEach {
            constructorBuilder.addParameter(getKexportParameterSpec(it))
        }
        return constructorBuilder.build()
    }

    private fun getKexportParameterSpec(kotlinValue: KotlinValue): ParameterSpec {
        return when (kotlinValue) {
            is KotlinProperty -> getKexportPropertyParameterSpec(kotlinValue)
            is KotlinFunction -> getKexportFunctionParameterSpec(kotlinValue)
        }
    }

    private fun getKexportPropertyParameterSpec(property: KotlinProperty): ParameterSpec {
        val propertyTypeName = property.property.returnType.toTypeName()
        val typeName = mapPropertyTypeName(propertyTypeName)
        return ParameterSpec.builder(property.simpleName, typeName)
            .build()
    }

    private fun getKexportFunctionParameterSpec(function: KotlinFunction): ParameterSpec {
        val propertyTypeName = function.returnType.toTypeName()
        val typeName = mapPropertyTypeName(propertyTypeName)
        return ParameterSpec.builder(function.simpleName, typeName)
            .build()
    }


    private fun mapPropertyTypeName(propertyTypeName: TypeName): TypeName {
        return exportableClasses
            .map { KexportableClass(it) }
            .filter { it.sourceClassName == propertyTypeName.asNonNullable() }
            .map { it.className.copy(nullable = propertyTypeName.isNullable) }
            .firstOrNull() ?: propertyTypeName
    }
}

@OptIn(KotlinPoetMetadataPreview::class)
internal fun KotlinProperty.getSerialName(namingConvention: Kexportable.NamingConvention): String {
        return propertyData.allAnnotations
            .firstOrNull { it.typeName == KexportName::class.asTypeName() }
            ?.getParameterValueAsString(KexportName::class.asTypeName(), "value")
            ?: getDefaultSerialName(namingConvention, simpleName)
}

internal fun KotlinFunction.getSerialName(namingConvention: Kexportable.NamingConvention): String =
    (getAnnotation(Kexportable::class) as Kexportable).exportName
        .ifBlank { getDefaultSerialName(namingConvention, simpleName) }

private fun getDefaultSerialName(namingConvention: Kexportable.NamingConvention, name: String): String {
    return when (namingConvention) {
        Kexportable.NamingConvention.AS_WRITTEN -> name
        Kexportable.NamingConvention.SNAKE_CASE -> name.toSnakeCase()
        else -> throw IllegalStateException(
            "ExportableClass must be initialized with Exportable annotation to get SerialName"
        )
    }
}
