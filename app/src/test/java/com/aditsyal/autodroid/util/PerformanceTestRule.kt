package com.aditsyal.autodroid.util

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

/**
 * JUnit TestRule that measures and logs the execution time of each test.
 * This helps identify slow tests that may need optimization.
 *
 * Usage:
 * @get:Rule
 * val performanceTestRule = PerformanceTestRule()
 *
 * Logs will appear in Timber with format:
 * "Test [testMethodName] took [duration]ms"
 */
class PerformanceTestRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val startTime = System.currentTimeMillis()
                try {
                    base.evaluate()
                } finally {
                    val duration = System.currentTimeMillis() - startTime
                    Timber.d("Test ${description.methodName} took ${duration}ms")
                }
            }
        }
    }
}
