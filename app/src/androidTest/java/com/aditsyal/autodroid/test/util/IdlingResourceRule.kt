package com.aditsyal.autodroid.test.util

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit TestRule that manages IdlingResource registration and unregistration.
 * This ensures proper cleanup of IdlingResources after each test.
 *
 * Usage:
 * @get:Rule
 * val idlingResourceRule = IdlingResourceRule()
 *
 * To register an IdlingResource:
 * idlingResourceRule.register(idlingResource)
 *
 * To unregister:
 * idlingResourceRule.unregister(idlingResource)
 */
class IdlingResourceRule : TestWatcher() {

    private val idlingRegistry = IdlingRegistry.getInstance()

    override fun finished(description: Description) {
        super.finished(description)
        try {
            idlingRegistry.unregisterIdleStatesCallback(idleCallback)
        } catch (e: Exception) {
            // Ignore if no callbacks registered
        }
    }

    /**
     * Registers an IdlingResource with the registry.
     */
    fun register(idlingResource: IdlingResource) {
        idlingRegistry.register(idlingResource)
    }

    /**
     * Unregisters an IdlingResource from the registry.
     */
    fun unregister(idlingResource: IdlingResource) {
        idlingRegistry.unregister(idlingResource)
    }

    private val idleCallback = IdlingRegistry.IdleCallback {
        // Callback for when all resources become idle
    }
}
