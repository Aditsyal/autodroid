package com.aditsyal.autodroid.test.util

import androidx.test.espresso.CountingIdlingResource

/**
 * Convenience wrapper for CountingIdlingResource that provides
 * easy integration with ViewModels and Use Cases for testing.
 *
 * Usage:
 * val idlingResource = TestIdlingResource("MacroExecution")
 *
 * In your ViewModel/UseCase:
 * - Increment when starting async operation
 * - Decrement when completing
 *
 * In tests:
 * idlingResource.increment()
 * // Perform action
 * idlingResource.decrement()
 * Espresso.onView(...).check(matches(isDisplayed()))
 */
class TestIdlingResource(private val name: String) : CountingIdlingResource(name) {

    /**
     * Increments the counter, indicating the resource is busy.
     */
    fun incrementBusy() {
        increment()
    }

    /**
     * Decrements the counter, indicating the resource is idle.
     */
    fun decrementBusy() {
        decrement()
    }

    /**
     * Wraps a suspend operation with idle resource management.
     */
    suspend fun <T> wrapOperation(operation: suspend () -> T): T {
        increment()
        return try {
            operation()
        } finally {
            decrement()
        }
    }
}
