package com.casadetasha.kexp.kexportable.processor

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.fail
import com.casadetasha.kexp.kexportable.processor.ktx.compileSource
import com.casadetasha.kexp.kexportable.processor.ktx.getAnnotationMethodForField
import com.casadetasha.kexp.kexportable.processor.ktx.hasOnlyDeclaredFields
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import kotlinx.serialization.SerialName
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.BeforeTest

class FunctionModelGenerationTest {
    companion object {
        private val source = SourceFile.kotlin(
            "Model.kt", """
            package com.casadetasha

            import com.casadetasha.kexp.kexportable.annotations.KexportName
            import com.casadetasha.kexp.kexportable.annotations.Kexportable

            @Kexportable
            class Model {
                @Kexportable
                fun testVal(): String = ""
                @Kexportable("export_named_test_val")
                fun exportedNameVal(): String? = null
                @Kexportable("sameNameVal")
                fun sameNameVal(): String? = null
                fun secretVal(): String? = null
              }
        """.trimIndent()
        )
    }

    private lateinit var compilationResult: KotlinCompilation.Result

    @BeforeTest
    fun `compile source`() {
        compilationResult = compileSource(source)
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
    fun `kexports only fields annotated with Kexportable`() {
        assertThat(compilationResult.exitCode).isEqualTo(OK)
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        assertThat(exportedClass).hasOnlyDeclaredFields("testVal", "exportedNameVal", "sameNameVal")
    }

    @Test
    fun `sets SerialName annotation for KexportNamed properties`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("exportedNameVal")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName).isNotNull()
    }

    @Test
    fun `does not set SerialName annotation for non-KexportNamed properties`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("testVal")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName).isNull()
    }

    @Test
    fun `does not set SerialName annotation for KexportNamed annotation that matches function name`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("secretVal")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName).isNull()
    }

    @Test
    fun `does not set SerialName annotation for KexportNamed properties that match the field name`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("testVal")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName).isNull()
    }

    @Test
    fun `sets kexported field SerialName as value of KexportName`() {
        val exportedClass = compilationResult.classLoader.loadClass("com.casadetasha.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("exportedNameVal")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName?.value).isEqualTo("export_named_test_val")
    }
}