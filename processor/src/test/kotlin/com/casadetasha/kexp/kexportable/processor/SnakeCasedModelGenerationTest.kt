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
              @SerialName("differentName")
              public val differentNameVal: String?,
              public val exportedNameVal: String?,
              @SerialName("test_val")
              public val testVal: String
            )

            public fun Model.kexport(): KexportedModel = KexportedModel(
              differentNameVal = differentNameVal,
              exportedNameVal = exportedNameVal,
              testVal = testVal
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

fun Assert<KotlinCompilation.Result>.hasExitCodeOK() = given { compilationResult ->
    if (compilationResult.exitCode != OK) fail(
            "Expected exitCode $OK but found ${compilationResult.exitCode}"
    )
}
