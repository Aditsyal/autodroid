package com.aditsyal.autodroid.di

import com.aditsyal.autodroid.data.repository.MacroRepositoryImpl
import com.aditsyal.autodroid.domain.repository.MacroRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMacroRepository(
        macroRepositoryImpl: MacroRepositoryImpl
    ): MacroRepository
}

