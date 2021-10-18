package com.casadetasha.kexp.kexportable.processor

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.fail
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import kotlinx.serialization.SerialName
import java.lang.reflect.Method
import kotlin.test.BeforeTest
import kotlin.test.Test

class ModelGenerationTest {
    companion object {
        private val source = SourceFile.kotlin("Model.kt", """
            package com.casadetasha

            import com.casadetasha.kexp.kexportable.annotations.KexportName
            import com.casadetasha.kexp.kexportable.annotations.Kexportable

            @Kexportable
            class Model {
                var testVal: String = ""
                @KexportName("export_named_test_val")
                var exportedNameVal: String? = null
                @Transient var secretVal: String? = null
              }
        """.trimIndent())
    }

    private lateinit var compilationResult: KotlinCompilation.Result

    @BeforeTest
    fun `compile source`() {
        compilationResult = KotlinCompilation().apply {
            sources = listOf(source)
            annotationProcessors = listOf(KexportableProcessor())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()
    }

    @Test
    fun `compiled with exit code OK`() {
        assertThat(compilationResult.exitCode).isEqualTo(OK)
    }

    @Test
    fun `sets source class name as SerialName of exported class`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")

        val annotation = exportedClass.getAnnotation(SerialName::class.java)
        assertThat(annotation).isNotNull()
        assertThat(annotation.value).isEqualTo("Model")
    }

    @Test
    fun `kexports only fields not annotated with Transient`() {
        assertThat(compilationResult.exitCode).isEqualTo(OK)
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        assertThat(exportedClass).hasOnlyDeclaredFields("testVal", "exportedNameVal")
    }

    @Test
    fun `sets SerialName annotation for KexportNamed properties`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("exportedNameVal")
        val exportedSerialName = fieldAnnotationMethod.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName).isNotNull()
    }

    @Test
    fun `sets kexported field SerialName as value of KexportName`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("exportedNameVal")
        val exportedSerialName = fieldAnnotationMethod.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName.value).isEqualTo("export_named_test_val")
    }

    private fun Class<*>.getAnnotationMethodForField(fieldName: String): Method {
        val annotatedGetterMethodName = getAnnotatedGetterMethodFromFieldName(fieldName)
        return getDeclaredMethod(annotatedGetterMethodName)
    }

    private fun getAnnotatedGetterMethodFromFieldName(fieldName: String): String {
        val uppercasedName = fieldName.replaceFirstChar { it.uppercase() }
        return "get$uppercasedName\$annotations"
    }
}

@Suppress("SameParameterValue")
private fun Assert<Class<*>>.hasOnlyDeclaredFields(vararg fieldNames : String) = given { clazz ->
    var mayHaveOnlyDeclaredFields = clazz.declaredFields.size == fieldNames.size
    fieldNames.forEach { name ->
        mayHaveOnlyDeclaredFields = mayHaveOnlyDeclaredFields and clazz.hasField(name)
    }
    if(mayHaveOnlyDeclaredFields) return else fail(
        "Expected declared fields to contain exactly ${fieldNames.toSortedSet()}, but found" +
                " ${clazz.declaredFields.map{ it.name }.toSortedSet()}"
    )
}

private fun Class<*>.hasField(fieldName: String): Boolean {
    return declaredFields.firstOrNull { it.name == fieldName } != null
}
