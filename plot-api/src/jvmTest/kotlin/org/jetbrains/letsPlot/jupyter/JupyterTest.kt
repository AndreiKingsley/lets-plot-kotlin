package org.jetbrains.letsPlot.jupyter

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.jetbrains.kotlinx.jupyter.api.Code
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResultEx
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.jetbrains.kotlinx.jupyter.testkit.ReplProvider
import org.junit.Rule
import org.junit.rules.TestName
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

internal abstract class JupyterTest : JupyterReplTestCase(
    ReplProvider.forLibrariesTesting(listOf("lets-plot-kotlin"))
) {

    @JvmField
    @Rule
    val testName: TestName = TestName()

    private val classLoader = (this::class as Any).javaClass.classLoader

    fun Code.checkCompilation() {
        execRendered(this)
    }

    fun assertOutput(actualOutputText: Any?) {
        if (actualOutputText !is MimeTypedResultEx) fail("The output is not MimeTypedResultEx")
        val outputDataObject = actualOutputText.toJson(JsonObject(mapOf()), null)["data"]?.jsonObject
        assertNotNull(outputDataObject)
        assertTrue(outputDataObject.contains("text/html"))
        assertTrue(outputDataObject.contains("application/plot+json"))

        val resourcePath = "jupyter/${testName.methodName}.out"
        val resource = classLoader.getResource(resourcePath)
        assertNotNull(resource)
        assertEquals(resource.readText(), outputDataObject["application/plot+json"].toString())
    }
}