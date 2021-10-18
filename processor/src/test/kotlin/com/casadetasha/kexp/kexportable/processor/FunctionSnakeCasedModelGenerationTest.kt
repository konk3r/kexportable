package com.casadetasha.kexp.kexportable.processor

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.fail
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test

class FunctionSnakeCasedModelGenerationTest {
    companion object {
        private val source = SourceFile.kotlin(
            "Model.kt", """
            package snake_case

            import com.casadetasha.kexp.kexportable.annotations.KexportName
            import com.casadetasha.kexp.kexportable.annotations.Kexportable
            import com.casadetasha.kexp.kexportable.annotations.Kexportable.NamingConvention.SNAKE_CASE

            @Kexportable(namingConvention = SNAKE_CASE)
            class Model {
                @Kexportable
                fun testFun(): String = ""
                @Kexportable("exportedNameFun")
                fun exportedNameFun(): String? = null
                @Kexportable("differentName")
                fun differentNameFun(): String? = null
                fun secretFun(): String? = null
              }
        """.trimIndent()
        )

        @Language("kotlin")
        private val expectedRawOutput: String = """
            package snake_case.kexport

            import kotlin.String
            import kotlinx.serialization.SerialName
            import kotlinx.serialization.Serializable
            import snake_case.Model

            @Serializable
            @SerialName("model")
            public data class KexportedModel(
              @SerialName("test_fun")
              public val testFun: String,
              public val exportedNameFun: String?,
              @SerialName("differentName")
              public val differentNameFun: String?
            )

            public fun Model.kexport(): KexportedModel = KexportedModel(
              exportedNameFun = exportedNameFun(),
              differentNameFun = differentNameFun(),
              testFun = testFun()
            )

        """.trimIndent()
    }

    private lateinit var compilationResult: KotlinCompilation.Result

    @Before
    fun `compile source`() {
        compilationResult = KotlinCompilation().apply {
            sources = listOf(source)
            annotationProcessors = listOf(KexportableProcessor())
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }

    @Test
    fun `compiled with exit code OK`() {
        assertThat(compilationResult).hasExitCodeOK()
    }

    @Test
    fun `actual raw output matches expected raw output`() {
        val file = compilationResult.sourcesGeneratedByAnnotationProcessor.firstOrNull()
        val actualRawOutput = file!!.inputStream().readBytes().toString(Charsets.UTF_8)

        assertThat(actualRawOutput).isEqualTo(expectedRawOutput)
    }
}
