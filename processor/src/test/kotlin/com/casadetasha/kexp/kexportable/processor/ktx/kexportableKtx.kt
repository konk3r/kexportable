package com.casadetasha.kexp.kexportable.processor.ktx

import assertk.Assert
import assertk.fail
import com.casadetasha.kexp.kexportable.processor.KexportableProcessor
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import java.lang.reflect.Method

@Suppress("SameParameterValue")
internal fun Assert<Class<*>>.hasOnlyDeclaredFields(vararg fieldNames : String) = given { clazz ->
    var mayHaveOnlyDeclaredFields = clazz.declaredFields.size == fieldNames.size
    fieldNames.forEach { name ->
        mayHaveOnlyDeclaredFields = mayHaveOnlyDeclaredFields and clazz.hasField(name)
    }
    if(mayHaveOnlyDeclaredFields) return else fail(
        "Expected declared fields to contain exactly ${fieldNames.toSortedSet()}, but found" +
                " ${clazz.declaredFields.map{ it.name }.toSortedSet()}"
    )
}

fun Class<*>.hasAnnotationMethodForField(fieldName: String): Boolean {
    return declaredMethods.any { it.name == fieldName }
}

internal fun Class<*>.getAnnotationMethodForField(fieldName: String): Method? {
    val annotatedGetterMethodName = getAnnotatedGetterMethodFromFieldName(fieldName)
    return declaredMethods.find { it.name == annotatedGetterMethodName }
}

internal fun getAnnotatedGetterMethodFromFieldName(fieldName: String): String {
    val uppercasedName = fieldName.replaceFirstChar { it.uppercase() }
    return "get$uppercasedName\$annotations"
}

internal fun Assert<KotlinCompilation.Result>.hasExitCodeOK() = given { compilationResult ->
    if (compilationResult.exitCode != KotlinCompilation.ExitCode.OK) fail(
        "Expected exitCode ${KotlinCompilation.ExitCode.OK} but found ${compilationResult.exitCode}"
    )
}

internal fun compileSource(sourceFile: SourceFile): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = listOf(sourceFile)
        annotationProcessors = listOf(KexportableProcessor())
        inheritClassPath = true
        messageOutputStream = System.out
    }.compile()
}

private fun Class<*>.hasField(fieldName: String): Boolean {
    return declaredFields.firstOrNull { it.name == fieldName } != null
}
