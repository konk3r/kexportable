package com.casadetasha.kexp.kexportable.processor

import com.casadetasha.kexp.kexportable.annotations.Kexportable
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassData
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.containerData
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@OptIn(KotlinPoetMetadataPreview::class)
fun RoundEnvironment?.generateClassMap(classInspector: ClassInspector): Map<ClassData, Element>? {
    if (this == null) return null

    val classToElementMap = HashMap<ClassData, Element>()
    getElementsAnnotatedWith(Kexportable::class.java)
        .forEach {
            val containerData = classInspector.containerData(it.getClassName(), null)
            check(containerData is ClassData) { "Unexpected container data type: ${containerData.javaClass}" }
            classToElementMap += containerData to it
        }
    return classToElementMap
}

@OptIn(KotlinPoetMetadataPreview::class)
fun Element.getClassName(): ClassName {
    val typeMetadata = getAnnotation(Metadata::class.java)
    val kmClass = typeMetadata.toImmutableKmClass()
    return ClassInspectorUtil.createClassName(kmClass.name)
}

@AutoService(Processor::class)
@SupportedOptions(KexportableProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@OptIn(KotlinPoetMetadataPreview::class)
class KexportableProcessor : AbstractProcessor() {
    private lateinit var classMap: Map<ClassData, Element>
    private lateinit var kaptKotlinGeneratedDir: String

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        Kexportable::class.java.canonicalName
    )

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return false

        val classInspector = ElementsClassInspector.create(processingEnv.elementUtils, processingEnv.typeUtils)
        classMap = roundEnv.generateClassMap(classInspector) ?: return false
        classMap.forEach {
            val kexportableAnnotation = it.value.getAnnotation(Kexportable::class.java)
            val kexportableClass = KexportableClass(it.key, kexportableAnnotation)
            KexportableClassFileGenerator(classMap.keys, kexportableClass)
                .writeToFile(kaptKotlinGeneratedDir)
        }
        return true
    }
}
