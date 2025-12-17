package com.aditsyal.autodroid.automation.trigger

import com.aditsyal.autodroid.data.models.TriggerDTO
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriggerManager @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards TriggerProvider>
) {
    private val providerMap = providers.associateBy { it.type }

    suspend fun registerTrigger(trigger: TriggerDTO) {
        val provider = providerMap[trigger.triggerType]
        if (provider != null) {
            Timber.d("Registering trigger ${trigger.id} of type ${trigger.triggerType}")
            provider.registerTrigger(trigger)
        } else {
            Timber.w("No provider found for trigger type: ${trigger.triggerType}")
        }
    }

    suspend fun unregisterTrigger(trigger: TriggerDTO) {
        val provider = providerMap[trigger.triggerType]
        if (provider != null) {
            Timber.d("Unregistering trigger ${trigger.id} of type ${trigger.triggerType}")
            provider.unregisterTrigger(trigger.id)
        }
    }

    suspend fun clearAllTriggers() {
        Timber.d("Clearing all triggers from all providers")
        providers.forEach { it.clearTriggers() }
    }
}
