package com.aditsyal.autodroid.test.annotation

/**
 * Marks a test as potentially flaky (non-deterministic or timing-dependent).
 * These tests may fail intermittently and should be monitored.
 *
 * Usage:
 * @FlakyTest
 * @Test
 * fun flakyNetworkTest() {
 *     // Network-dependent test that may timeout
 * }
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class FlakyTest(
    val reason: String = "",
    val maxAttempts: Int = 3
)
