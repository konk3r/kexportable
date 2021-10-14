package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.AnnotationParser
import com.casadetasha.kexp.annotationparser.kxt.getClassesAnnotatedWith
import com.casadetasha.kexp.kexportable.annotations.Kexportable
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedOptions(KexportableProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@OptIn(KotlinPoetMetadataPreview::class)
class KexportableProcessor : AbstractProcessor() {
    private lateinit var kaptKotlinGeneratedDir: String

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        Kexportable::class.java.canonicalName
    )

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return false
        if (roundEnv == null) return false
        AnnotationParser.setup(processingEnv)

        generateClasses(roundEnv)
        return true
    }

    private fun generateClasses(roundEnv: RoundEnvironment) {
        val kexportedClasses = roundEnv.getClassesAnnotatedWith(Kexportable::class)
        kexportedClasses.forEach { kotlinClass ->
            val kexportableAnnotation = kotlinClass.getAnnotation(Kexportable::class)!!
            val kexportableClass = KexportableClass(kotlinClass, kexportableAnnotation as Kexportable)

            KexportableClassFileGenerator(kexportedClasses,kexportableClass).writeToFile(kaptKotlinGeneratedDir)
        }
    }
}
