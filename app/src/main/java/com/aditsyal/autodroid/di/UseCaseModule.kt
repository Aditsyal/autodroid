package com.aditsyal.autodroid.di

import android.content.Context
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
    fun provideCreateMacroUseCase(repository: MacroRepository): CreateMacroUseCase =
        CreateMacroUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateMacroUseCase(repository: MacroRepository): UpdateMacroUseCase =
        UpdateMacroUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteMacroUseCase(repository: MacroRepository): DeleteMacroUseCase =
        DeleteMacroUseCase(repository)

    @Provides
    @Singleton
    fun provideToggleMacroUseCase(repository: MacroRepository): ToggleMacroUseCase =
        ToggleMacroUseCase(repository)

    @Provides
    @Singleton
    fun provideExecuteMacroUseCase(repository: MacroRepository): ExecuteMacroUseCase =
        ExecuteMacroUseCase(repository)

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
}


