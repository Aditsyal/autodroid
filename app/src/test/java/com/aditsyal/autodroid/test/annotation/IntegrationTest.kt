package com.aditsyal.autodroid.test.annotation

/**
 * Marks a test as an integration test that tests multiple components together.
 * Integration tests typically run slower and may require additional setup.
 *
 * Usage:
 * @IntegrationTest
 * @Test
 * fun repositoryIntegrationTest() {
 *     // Tests repository with actual database
 * }
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class IntegrationTest(
    val type: String = "general"
)
