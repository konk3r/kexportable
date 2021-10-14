package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.casadetasha.kexp.annotationparser.KotlinFunction
import com.casadetasha.kexp.kexportable.processor.kxt.asNonNullable
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

        kexportableClass.exportableProperties.forEach {
            classTypeBuilder.addProperty(kexportableClass.getExportedPropertySpec(it))
        }

        kexportableClass.exportableFunctions.forEach {
            classTypeBuilder.addProperty(kexportableClass.getExportedFunctionSpec(it))
        }

        return classTypeBuilder
            .primaryConstructor(kexportableClass.toConstructorSpec())
            .build()
    }

    private fun getExportDataParameterSpec(property: ImmutableKmProperty): ParameterSpec {
        val propertyTypeName = property.returnType.toTypeName()
        val typeName = mapPropertyTypeName(propertyTypeName)
        return ParameterSpec.builder(property.name, typeName)
            .build()
    }

    private fun getExportDataParameterSpec(function: KotlinFunction): ParameterSpec {
        val propertyTypeName = function.returnType.toTypeName()
        val typeName = mapPropertyTypeName(propertyTypeName)
        return ParameterSpec.builder(function.simpleName, typeName)
            .build()
    }

    private fun KexportableClass.getExportedPropertySpec(property: ImmutableKmProperty): PropertySpec {
        val propertyTypeName = property.returnType.toTypeName()
        val propertyBuilder = PropertySpec.builder(property.name, mapPropertyTypeName(propertyTypeName))
        val serialName = property.getSerialName()

        if (serialName != property.name) {
            propertyBuilder.addAnnotation(
                AnnotationSpec.builder(SerialName::class)
                    .addMember("%S", serialName)
                    .build()
            )
        }

        return propertyBuilder
            .initializer(property.name)
            .build()
    }

    private fun KexportableClass.getExportedFunctionSpec(function: KotlinFunction): PropertySpec {
        val propertyTypeName = function.returnType.toTypeName()
        val propertyBuilder = PropertySpec.builder(function.simpleName, mapPropertyTypeName(propertyTypeName))
        val serialName = function.getSerialName()

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

    private fun mapPropertyTypeName(propertyTypeName: TypeName): TypeName {
        return exportableClasses
            .map { KexportableClass(it) }
            .filter { it.sourceClassName == propertyTypeName.asNonNullable() }
            .map { it.className.copy(nullable = propertyTypeName.isNullable) }
            .firstOrNull() ?: propertyTypeName
    }

    private fun KexportableClass.toConstructorSpec(): FunSpec {
        val constructorBuilder = FunSpec.constructorBuilder()
        exportableProperties.forEach {
            constructorBuilder.addParameter(getExportDataParameterSpec(it))
        }
        exportableFunctions.forEach {
            constructorBuilder.addParameter(getExportDataParameterSpec(it))
        }
        return constructorBuilder.build()
    }
}
