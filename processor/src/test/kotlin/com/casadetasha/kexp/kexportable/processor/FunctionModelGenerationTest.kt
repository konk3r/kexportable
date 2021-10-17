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
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.BeforeTest

class FunctionModelGenerationTest {
    companion object {
        private val source = SourceFile.kotlin("Model.kt", """
            package com.casadetasha

            import com.casadetasha.kexp.kexportable.annotations.KexportName
            import com.casadetasha.kexp.kexportable.annotations.Kexportable

            @Kexportable
            class Model {
                @Kexportable
                fun testVal(): String = ""
                @Kexportable("export_named_test_val")
                fun exportedNameVal(): String? = null
                fun secretVal(): String? = null
              }
        """.trimIndent())

        @Language("kotlin")
        private val expectedRawOutput: String = """
            package com.casadetasha.kexport

            import com.casadetasha.Model
            import kotlin.String
            import kotlinx.serialization.SerialName
            import kotlinx.serialization.Serializable

            @Serializable
            @SerialName("Model")
            public data class KexportedModel(
              @SerialName("export_named_test_val")
              public val exportedNameVal: String?,
              public val testVal: String
            )

            public fun Model.kexport(): KexportedModel = KexportedModel(
              exportedNameVal = exportedNameVal(),
              testVal = testVal()
            )

        """.trimIndent()
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
    fun `actual raw output matches expected raw output`() {
        assertThat(compilationResult.exitCode).isEqualTo(OK)
        val file = compilationResult.sourcesGeneratedByAnnotationProcessor.firstOrNull()
        val actualRawOutput = file!!.inputStream().readBytes().toString(Charsets.UTF_8)

        assertThat(actualRawOutput).isEqualTo(expectedRawOutput)
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
        assertThat(exportedClass).hasOnlyDeclaredFields("testVal", "exportedNameVal")
    }
}

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
