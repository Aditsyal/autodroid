package com.aditsyal.autodroid.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * JUnit TestRule that instant executes all Architecture Components background
 * tasks synchronously on the same thread as the test.
 *
 * This is essential for testing LiveData and other Architecture Components
 * that involve background threading.
 *
 * Usage:
 * @get:Rule
 * val instantTaskExecutorRule = InstantTaskExecutorRule()
 */
class InstantTaskExecutorRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val rule = InstantTaskExecutorRule()
                rule.apply(base, description).evaluate()
            }
        }
    }
}
