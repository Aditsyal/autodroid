package com.aditsyal.autodroid.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Test rule that sets up the main dispatcher for testing coroutines.
 * This replaces the default Main dispatcher with a test dispatcher.
 *
 * Usage:
 * @get:Rule
 * val testCoroutineRule = TestCoroutineRule()
 *
 * @Test
 * fun `test with coroutines`() = runTest {
 *     // Test code that uses viewModelScope.launch, etc.
 * }
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TestCoroutineRule : TestWatcher() {
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}