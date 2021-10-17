package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.casadetasha.kexp.annotationparser.KotlinValue.KotlinFunction
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
        val statementParser = KexportKtxFunctionParser(exportableClasses, kexportableClass)
        return FunSpec.builder(EXPORT_METHOD_SIMPLE_NAME)
            .returns(kexportableClass.className)
            .receiver(kexportableClass.sourceClassName)
            .addStatement(statementParser.exportMethodStatement)
            .build()
    }

    private class KexportKtxFunctionParser(private val exportableClasses: Set<KotlinContainer.KotlinClass>,
                                           private val kexportableClass: KexportableClass
    ) {
        private val stringBuilder = StringBuilder()

        val exportMethodStatement: String by lazy {
            stringBuilder.append("return ${kexportableClass.classSimpleName}(")
            amendSettersForNonKexportedParams()
            amendSettersForKexportedParams()
            amendSettersForNonKexportedFunctions()
            amendSettersForKexportedFunctions()
            closeExportCreation()
            stringBuilder.toString()
        }

        private fun amendSettersForNonKexportedParams(): StringBuilder {
            kexportableClass.kexportableProperties
                .filterNot { exportableClasses.containsMatchingType(it.property.returnType) }
                .forEach { stringBuilder.append(it.property.asConstructorBlock()) }
            return stringBuilder
        }

        private fun amendSettersForKexportedParams(): StringBuilder {
            kexportableClass.kexportableProperties
                .filter { exportableClasses.containsMatchingType(it.property.returnType) }
                .forEach { stringBuilder.append(it.property.asKexportedConstructorBlock()) }
            return stringBuilder
        }

        private fun amendSettersForNonKexportedFunctions(): StringBuilder {
            kexportableClass.kexportableFunctions
                .filterNot { exportableClasses.containsMatchingType(it.returnType) }
                .forEach { stringBuilder.append(it.asConstructorBlock()) }
            return stringBuilder
        }

        private fun amendSettersForKexportedFunctions(): StringBuilder {
            kexportableClass.kexportableFunctions
                .filter { exportableClasses.containsMatchingType(it.returnType) }
                .forEach { stringBuilder.append(it.asKexportedConstructorBlock()) }
            return stringBuilder
        }

        private fun closeExportCreation(): StringBuilder {
            stringBuilder.removeTrailingComma()
            stringBuilder.append("\n)")
            return stringBuilder
        }

        private fun ImmutableKmProperty.asConstructorBlock() = "\n  $name = ${name},"

        private fun KotlinFunction.asConstructorBlock() = "\n  $simpleName = ${simpleName}(),"

        private fun ImmutableKmProperty.asKexportedConstructorBlock(): String {
            val nullabilityBlock = if (returnType.isNullable) "?" else ""
            return "\n  $name = ${name}${nullabilityBlock}.${EXPORT_METHOD_SIMPLE_NAME}(),"
        }

        private fun KotlinFunction.asKexportedConstructorBlock(): String {
            val nullabilityBlock = if (returnType.isNullable) "?" else ""
            return "\n  $simpleName = ${simpleName}()${nullabilityBlock}.${EXPORT_METHOD_SIMPLE_NAME}(),"
        }
    }
}
