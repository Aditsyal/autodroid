package com.aditsyal.autodroid.util

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.core.internal.deps.guava.base.Preconditions
import org.junit.rules.ExternalResource
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * JUnit TestRule that provides ActivityScenario management for tests.
 * This is useful for instrumented tests that need to interact with Activities.
 *
 * Usage:
 * @get:Rule
 * val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)
 *
 * @Test
 * fun testActivity() {
 *     activityScenarioRule.scenario.onActivity { activity ->
 *         // Test activity
 *     }
 * }
 */
class ActivityScenarioRule<A : androidx.appcompat.app.AppCompatActivity>(
    private val activityClass: Class<A>
) : ExternalResource() {

    private var scenario: ActivityScenario<A>? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                before()
                try {
                    base.evaluate()
                } finally {
                    after()
                }
            }
        }
    }

    override fun before() {
        scenario = launch(activityClass)
    }

    override fun after() {
        scenario?.close()
        scenario = null
    }

    /**
     * Returns the ActivityScenario for interaction.
     */
    val scenario: ActivityScenario<A>
        get() = Preconditions.checkNotNull(scenario, "ActivityScenario not initialized")
}
