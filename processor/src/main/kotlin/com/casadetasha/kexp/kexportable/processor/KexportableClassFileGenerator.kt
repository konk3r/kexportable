package com.casadetasha.kexp.kexportable.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassData
import java.io.File

@OptIn(KotlinPoetMetadataPreview::class)
internal class KexportableClassFileGenerator(private val exportableClasses: Set<ClassData>,
                                             private val kexportableClass: KexportableClass
) {

    fun writeToFile(directoryPath: String) {
        val classSpecBuilder = KexportedClassSpecBuilder(exportableClasses)
        val funSpecBuilder = ExportedFunSpecBuilder(exportableClasses)
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