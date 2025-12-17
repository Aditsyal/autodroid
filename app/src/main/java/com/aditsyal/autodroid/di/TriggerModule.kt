package com.aditsyal.autodroid.di

import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.LocationTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.SensorTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.TimeTriggerProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TriggerModule {

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindTimeTriggerProvider(provider: TimeTriggerProvider): TriggerProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindLocationTriggerProvider(provider: LocationTriggerProvider): TriggerProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindSensorTriggerProvider(provider: SensorTriggerProvider): TriggerProvider
}
