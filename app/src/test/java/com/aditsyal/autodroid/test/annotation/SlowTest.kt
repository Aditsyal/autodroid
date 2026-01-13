package com.aditsyal.autodroid.test.annotation

/**
 * Marks a test as a slow/integration test that should be excluded from
 * normal test runs and only run manually or in specific CI pipelines.
 *
 * Usage:
 * @SlowTest
 * @Test
 * fun slowIntegrationTest() {
 *     // Long-running test
 * }
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class SlowTest(
    val reason: String = ""
)
