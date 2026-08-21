package com.rendox.shoppinggenius

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ShoppingGeniusBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun ShoppingGeniusBenchmarkCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun ShoppingGeniusBenchmarkCompilationBaselineProfile() = benchmark(CompilationMode.Partial())

    @Test
    fun ShoppingGeniusBenchmarkCompilationFull() = benchmark(CompilationMode.Full())

    private fun benchmark(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.rendox.shoppinggenius",
        metrics = listOf(FrameTimingMetric()),
        iterations = 8,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        },
        compilationMode = compilationMode,
    ) {
        val groceryListName = "TestGroceryList"
        createNewGroceryList(groceryListName)
        navigateToCategory()
        addGroceries()
        device.pressBack()
        device.waitForIdle()
        device.pressBack()
        navigateToSettings()
        device.pressBack()
        navigateToGroceryList(groceryListName)
        device.waitForIdle()
        deleteGroceryList()
        device.waitForIdle()
    }
}
