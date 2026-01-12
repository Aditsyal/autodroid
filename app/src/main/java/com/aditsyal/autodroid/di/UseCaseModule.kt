package com.aditsyal.autodroid.di

import android.content.Context
import com.aditsyal.autodroid.automation.trigger.TriggerManager
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import com.aditsyal.autodroid.domain.usecase.ManageBatteryOptimizationUseCase
import com.aditsyal.autodroid.domain.usecase.CreateMacroUseCase
import com.aditsyal.autodroid.domain.usecase.DeleteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.ExecuteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.GetAllMacrosUseCase
import com.aditsyal.autodroid.domain.usecase.GetMacroByIdUseCase
import com.aditsyal.autodroid.domain.usecase.ToggleMacroUseCase
import com.aditsyal.autodroid.domain.usecase.UpdateMacroUseCase
import com.aditsyal.autodroid.domain.usecase.EvaluateConstraintsUseCase
import com.aditsyal.autodroid.domain.usecase.ExecuteActionUseCase
import com.aditsyal.autodroid.utils.PerformanceMonitor
import com.aditsyal.autodroid.domain.usecase.GetVariableUseCase
import com.aditsyal.autodroid.domain.usecase.SetVariableUseCase
import com.aditsyal.autodroid.domain.usecase.EvaluateVariableUseCase
import com.aditsyal.autodroid.domain.usecase.EvaluateLogicUseCase
import com.aditsyal.autodroid.domain.usecase.CreateMacroFromTemplateUseCase
import com.aditsyal.autodroid.domain.usecase.InitializeDefaultTemplatesUseCase
import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.dao.VariableDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetAllMacrosUseCase(repository: MacroRepository): GetAllMacrosUseCase =
        GetAllMacrosUseCase(repository)

    @Provides
    @Singleton
    fun provideGetMacroByIdUseCase(repository: MacroRepository): GetMacroByIdUseCase =
        GetMacroByIdUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateMacroUseCase(
        repository: MacroRepository,
        triggerManager: TriggerManager
    ): CreateMacroUseCase =
        CreateMacroUseCase(repository, triggerManager)

    @Provides
    @Singleton
    fun provideUpdateMacroUseCase(
        repository: MacroRepository,
        triggerManager: TriggerManager
    ): UpdateMacroUseCase =
        UpdateMacroUseCase(repository, triggerManager)

    @Provides
    @Singleton
    fun provideDeleteMacroUseCase(repository: MacroRepository): DeleteMacroUseCase =
        DeleteMacroUseCase(repository)

    @Provides
    @Singleton
    fun provideToggleMacroUseCase(
        repository: MacroRepository,
        triggerManager: TriggerManager
    ): ToggleMacroUseCase =
        ToggleMacroUseCase(repository, triggerManager)

    @Provides
    @Singleton
    fun provideEvaluateConstraintsUseCase(@ApplicationContext context: Context): EvaluateConstraintsUseCase =
        EvaluateConstraintsUseCase(context)

    @Provides
    @Singleton
    fun provideExecuteActionUseCase(
        @ApplicationContext context: Context,
        getVariableUseCase: GetVariableUseCase,
        setVariableUseCase: SetVariableUseCase,
        evaluateVariableUseCase: EvaluateVariableUseCase,
        wifiToggleExecutor: com.aditsyal.autodroid.domain.usecase.executors.WifiToggleExecutor,
        bluetoothToggleExecutor: com.aditsyal.autodroid.domain.usecase.executors.BluetoothToggleExecutor,
        volumeControlExecutor: com.aditsyal.autodroid.domain.usecase.executors.VolumeControlExecutor,
        notificationExecutor: com.aditsyal.autodroid.domain.usecase.executors.NotificationExecutor,
        sendSmsExecutor: com.aditsyal.autodroid.domain.usecase.executors.SendSmsExecutor,
        launchAppExecutor: com.aditsyal.autodroid.domain.usecase.executors.LaunchAppExecutor,
        openUrlExecutor: com.aditsyal.autodroid.domain.usecase.executors.OpenUrlExecutor,
        setBrightnessExecutor: com.aditsyal.autodroid.domain.usecase.executors.SetBrightnessExecutor,
        toggleAirplaneModeExecutor: com.aditsyal.autodroid.domain.usecase.executors.ToggleAirplaneModeExecutor,
        tetheringExecutor: com.aditsyal.autodroid.domain.usecase.executors.TetheringExecutor,
        delayExecutor: com.aditsyal.autodroid.domain.usecase.executors.DelayExecutor,
        toastExecutor: com.aditsyal.autodroid.domain.usecase.executors.ToastExecutor,
        vibrateExecutor: com.aditsyal.autodroid.domain.usecase.executors.VibrateExecutor,
        playSoundExecutor: com.aditsyal.autodroid.domain.usecase.executors.PlaySoundExecutor,
        stopSoundExecutor: com.aditsyal.autodroid.domain.usecase.executors.StopSoundExecutor,
        // New Phase 1 executors
        httpRequestExecutor: com.aditsyal.autodroid.domain.usecase.executors.HttpRequestExecutor,
        lockScreenExecutor: com.aditsyal.autodroid.domain.usecase.executors.LockScreenExecutor,
        ttsExecutor: com.aditsyal.autodroid.domain.usecase.executors.TtsExecutor,
        screenTimeoutExecutor: com.aditsyal.autodroid.domain.usecase.executors.ScreenTimeoutExecutor,
        dndExecutor: com.aditsyal.autodroid.domain.usecase.executors.DndExecutor,
        mediaControlExecutor: com.aditsyal.autodroid.domain.usecase.executors.MediaControlExecutor,
        closeAppExecutor: com.aditsyal.autodroid.domain.usecase.executors.CloseAppExecutor,
        clearCacheExecutor: com.aditsyal.autodroid.domain.usecase.executors.ClearCacheExecutor,
        unlockScreenExecutor: com.aditsyal.autodroid.domain.usecase.executors.UnlockScreenExecutor,
        startMusicExecutor: com.aditsyal.autodroid.domain.usecase.executors.StartMusicExecutor,
        closeNotificationExecutor: com.aditsyal.autodroid.domain.usecase.executors.CloseNotificationExecutor,
        setRingtoneExecutor: com.aditsyal.autodroid.domain.usecase.executors.SetRingtoneExecutor,
        deleteSmsExecutor: com.aditsyal.autodroid.domain.usecase.executors.DeleteSmsExecutor,
        makeCallExecutor: com.aditsyal.autodroid.domain.usecase.executors.MakeCallExecutor
    ): ExecuteActionUseCase =
        ExecuteActionUseCase(
            context,
            getVariableUseCase,
            setVariableUseCase,
            evaluateVariableUseCase,
            wifiToggleExecutor,
            bluetoothToggleExecutor,
            volumeControlExecutor,
            notificationExecutor,
            sendSmsExecutor,
            launchAppExecutor,
            openUrlExecutor,
            setBrightnessExecutor,
            toggleAirplaneModeExecutor,
            tetheringExecutor,
            delayExecutor,
            toastExecutor,
            vibrateExecutor,
            playSoundExecutor,
            stopSoundExecutor,
            // New Phase 1 executors
            httpRequestExecutor,
            lockScreenExecutor,
            ttsExecutor,
            screenTimeoutExecutor,
            dndExecutor,
            mediaControlExecutor,
            closeAppExecutor,
            clearCacheExecutor,
            unlockScreenExecutor,
            startMusicExecutor,
            closeNotificationExecutor,
            setRingtoneExecutor,
            deleteSmsExecutor,
            makeCallExecutor
        )

    @Provides
    @Singleton
    fun provideEvaluateLogicUseCase(getVariableUseCase: GetVariableUseCase): EvaluateLogicUseCase =
        EvaluateLogicUseCase(getVariableUseCase)

    @Provides
    @Singleton
    fun provideExecuteMacroUseCase(
        repository: MacroRepository,
        evaluateConstraintsUseCase: EvaluateConstraintsUseCase,
        executeActionUseCase: ExecuteActionUseCase,
        evaluateLogicUseCase: EvaluateLogicUseCase,
        performanceMonitor: PerformanceMonitor
    ): ExecuteMacroUseCase =
        ExecuteMacroUseCase(repository, evaluateConstraintsUseCase, executeActionUseCase, evaluateLogicUseCase, performanceMonitor)

    @Provides
    @Singleton
    fun provideCheckTriggersUseCase(
        repository: MacroRepository,
        executeMacroUseCase: ExecuteMacroUseCase
    ): CheckTriggersUseCase =
        CheckTriggersUseCase(repository, executeMacroUseCase)

    @Provides
    @Singleton
    fun provideCheckPermissionsUseCase(@ApplicationContext context: Context): CheckPermissionsUseCase =
        CheckPermissionsUseCase(context)

    @Provides
    @Singleton
    fun provideManageBatteryOptimizationUseCase(@ApplicationContext context: Context): ManageBatteryOptimizationUseCase =
        ManageBatteryOptimizationUseCase(context)

    @Provides
    @Singleton
    fun provideGetVariableUseCase(variableDao: VariableDao): GetVariableUseCase =
        GetVariableUseCase(variableDao)

    @Provides
    @Singleton
    fun provideSetVariableUseCase(variableDao: VariableDao): SetVariableUseCase =
        SetVariableUseCase(variableDao)

    @Provides
    @Singleton
    fun provideEvaluateVariableUseCase(getVariableUseCase: GetVariableUseCase): EvaluateVariableUseCase =
        EvaluateVariableUseCase(getVariableUseCase)

    @Provides
    @Singleton
    fun provideCreateMacroFromTemplateUseCase(
        templateDao: TemplateDao,
        createMacroUseCase: CreateMacroUseCase
    ): CreateMacroFromTemplateUseCase =
        CreateMacroFromTemplateUseCase(templateDao, createMacroUseCase)

    @Provides
    @Singleton
    fun provideInitializeDefaultTemplatesUseCase(templateDao: TemplateDao): InitializeDefaultTemplatesUseCase =
        InitializeDefaultTemplatesUseCase(templateDao)
}


