package com.aditsyal.autodroid.di

import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.AppEventTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.CommunicationTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.ConnectivityTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.DeviceStateTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.LocationTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.MusicTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.SensorTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.TimeTriggerProvider
import com.aditsyal.autodroid.automation.trigger.providers.UsbTriggerProvider
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

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindDeviceStateTriggerProvider(provider: DeviceStateTriggerProvider): TriggerProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindConnectivityTriggerProvider(provider: ConnectivityTriggerProvider): TriggerProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindAppEventTriggerProvider(provider: AppEventTriggerProvider): TriggerProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindCommunicationTriggerProvider(provider: CommunicationTriggerProvider): TriggerProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindMusicTriggerProvider(provider: MusicTriggerProvider): TriggerProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindUsbTriggerProvider(provider: UsbTriggerProvider): TriggerProvider
}


