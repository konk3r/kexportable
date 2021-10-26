package com.casadetasha.kexp.kexportable.processor

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.casadetasha.kexp.kexportable.processor.ktx.compileSource
import com.casadetasha.kexp.kexportable.processor.ktx.getAnnotationMethodForField
import com.casadetasha.kexp.kexportable.processor.ktx.hasExitCodeOK
import com.casadetasha.kexp.kexportable.processor.ktx.hasOnlyDeclaredFields
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import kotlinx.serialization.SerialName
import org.junit.Before
import org.junit.Test

class SnakeCasedModelGenerationTest {

    companion object {

        private val source = SourceFile.kotlin(
            "Model.kt", """
            package snake_case

            import com.casadetasha.kexp.kexportable.annotations.KexportName
            import com.casadetasha.kexp.kexportable.annotations.Kexportable
            import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.SNAKE_CASE

            @Kexportable(namingConvention = SNAKE_CASE)
            class Model {
                var testVal: String = ""
                @KexportName("exportedNameVal")
                var exportedNameVal: String? = null
                @KexportName("differentName")
                var differentNameVal: String? = null
                @Transient var secretVal: String? = null
              }
        """.trimIndent()
        )
    }


    private lateinit var compilationResult: KotlinCompilation.Result

    @Before
    fun `compile source`() {
        compilationResult = compileSource(source)
    }

    @Test
    fun `compiled with exit code OK`() {
        assertThat(compilationResult).hasExitCodeOK()
    }

    @Test
    fun `sets source class name as SerialName of exported class`() {
        val exportedClass = compilationResult.classLoader.loadClass("snake_case.kexport.KexportedModel")

        val annotation = exportedClass.getAnnotation(SerialName::class.java)
        assertThat(annotation).isNotNull()
        assertThat(annotation.value).isEqualTo("model")
    }

    @Test
    fun `kexports only fields not annotated with Transient`() {
        assertThat(compilationResult.exitCode).isEqualTo(OK)
        val exportedClass = compilationResult.classLoader.loadClass("snake_case.kexport.KexportedModel")
        assertThat(exportedClass).hasOnlyDeclaredFields("testVal", "exportedNameVal", "differentNameVal")
    }

    @Test
    fun `does not set SerialName annotation for KexportNamed functions with the field name`() {
        val exportedClass = compilationResult.classLoader.loadClass("snake_case.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("exportedNameVal")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName).isNull()
    }

    @Test
    fun `sets SerialName annotation for KexportNamed functions`() {
        val exportedClass = compilationResult.classLoader.loadClass("snake_case.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("differentName")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName).isNull()
    }

    @Test
    fun `sets kexported field SerialName as the snake_case value of the field name`() {
        val exportedClass = compilationResult.classLoader.loadClass("snake_case.kexport.KexportedModel")
        val fieldAnnotationMethod = exportedClass.getAnnotationMethodForField("testVal")
        val exportedSerialName = fieldAnnotationMethod?.getAnnotation(SerialName::class.java)

        assertThat(exportedSerialName?.value).isEqualTo("test_val")
    }
}
