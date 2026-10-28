package com.example

import com.example.ai.GeminiNexusBrain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusBrainUnitTest {

    @Test
    fun testPythonSandboxArithmetic() {
        val brain = GeminiNexusBrain()
        val result = brain.executePythonSandbox("2 + 2 * 10")
        assertNotNull(result)
        assertTrue(result.output.contains("Computed"))
        assertTrue(result.chartData.isNotEmpty())
    }

    @Test
    fun testPythonSandboxPlotSimulation() {
        val brain = GeminiNexusBrain()
        val script = """
            import matplotlib.pyplot as plt
            data = [10, 20, 30]
            plt.plot(data)
        """.trimIndent()
        val result = brain.executePythonSandbox(script)
        assertNotNull(result)
        assertTrue(result.output.contains("Python 3.12"))
        assertEquals(5, result.chartData.size)
    }
}
