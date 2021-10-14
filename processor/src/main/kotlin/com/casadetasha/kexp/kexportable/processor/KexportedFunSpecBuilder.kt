package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.casadetasha.kexp.kexportable.processor.KexportableClass.Companion.EXPORT_METHOD_SIMPLE_NAME
import com.casadetasha.kexp.kexportable.processor.kxt.containsMatchingType
import com.casadetasha.kexp.kexportable.processor.kxt.removeTrailingComma
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable

@OptIn(KotlinPoetMetadataPreview::class)
internal class KexportedFunSpecBuilder(private val exportableClasses: Set<KotlinContainer.KotlinClass>) {

    fun getFunSpec(kexportableClass: KexportableClass): FunSpec {
        val statementParser = MethodStatementParser(exportableClasses, kexportableClass)
        return FunSpec.builder(EXPORT_METHOD_SIMPLE_NAME)
            .returns(kexportableClass.className)
            .receiver(kexportableClass.sourceClassName)
            .addStatement(statementParser.exportMethodStatement)
            .build()
    }

    private class MethodStatementParser(private val exportableClasses: Set<KotlinContainer.KotlinClass>,
                                        private val kexportableClass: KexportableClass
    ) {
        private val stringBuilder = StringBuilder()

        val exportMethodStatement: String by lazy {
            stringBuilder.append("return ${kexportableClass.classSimpleName}(")
            amendSettersForNonExportedParams()
            amendSettersForExportedParams()
            closeExportCreation()
            stringBuilder.toString()
        }

        private fun amendSettersForNonExportedParams(): StringBuilder {
            kexportableClass.exportableProperties
                .filterNot { exportableClasses.containsMatchingType(it.returnType) }
                .forEach { stringBuilder.append(it.asConstructorBlock()) }
            return stringBuilder
        }

        private fun amendSettersForExportedParams(): StringBuilder {
            kexportableClass.exportableProperties
                .filter { exportableClasses.containsMatchingType(it.returnType) }
                .forEach { stringBuilder.append(it.asExportedConstructorBlock()) }
            return stringBuilder
        }

        private fun closeExportCreation(): StringBuilder {
            stringBuilder.removeTrailingComma()
            stringBuilder.append("\n)")
            return stringBuilder
        }

        private fun ImmutableKmProperty.asConstructorBlock() = "\n  $name = ${name},"

        private fun ImmutableKmProperty.asExportedConstructorBlock(): String {
            val nullabilityBlock = if (returnType.isNullable) "?" else ""
            return "\n  $name = ${name}${nullabilityBlock}.${EXPORT_METHOD_SIMPLE_NAME}(),"
        }
    }
}
