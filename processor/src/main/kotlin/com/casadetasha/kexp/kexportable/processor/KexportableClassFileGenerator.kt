package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.KotlinContainer
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import java.io.File

@OptIn(KotlinPoetMetadataPreview::class)
internal class KexportableClassFileGenerator(
    private val exportableClasses: Set<KotlinContainer.KotlinClass>,
    private val kexportableClass: KexportableClass
) {

    fun writeToFile(directoryPath: String) {
        val classSpecBuilder = KexportedClassSpecBuilder(exportableClasses)
        val funSpecBuilder = KexportedFunSpecBuilder(exportableClasses)
        val fileSpec = FileSpec.builder(
            packageName = kexportableClass.packageName,
            fileName = kexportableClass.classSimpleName
        )
            .addType(classSpecBuilder.getClassSpec(kexportableClass))
            .addFunction(funSpecBuilder.getFunSpec(kexportableClass))
            .build()

        fileSpec.writeTo(File(directoryPath))
    }
}