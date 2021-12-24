package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.annotationparser.AnnotationParser
import com.casadetasha.kexp.annotationparser.AnnotationParser.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.casadetasha.kexp.annotationparser.AnnotationParser.getClassesAnnotatedWith
import com.casadetasha.kexp.annotationparser.AnnotationParser.kaptKotlinGeneratedDir
import com.casadetasha.kexp.kexportable.annotations.Kexportable
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@OptIn(KotlinPoetMetadataPreview::class)
class KexportableProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        Kexportable::class.java.canonicalName
    )

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        try {
            AnnotationParser.setup(processingEnv, roundEnv)
        } catch (_: IllegalStateException) {
            return false
        }

        generateClasses()
        return true
    }

    private fun generateClasses() {
        val kexportedClasses = getClassesAnnotatedWith(Kexportable::class)
        kexportedClasses.forEach { kotlinClass ->
            val kexportableAnnotation = kotlinClass.getAnnotation(Kexportable::class)!!
            val kexportableClass = KexportableClass(kotlinClass, kexportableAnnotation as Kexportable)

            KexportableClassFileGenerator(kexportedClasses, kexportableClass).writeToFile(kaptKotlinGeneratedDir)
        }
    }
}
