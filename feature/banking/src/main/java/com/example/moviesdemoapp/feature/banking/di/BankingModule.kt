package com.example.moviesdemoapp.feature.banking.di

import com.example.moviesdemoapp.feature.banking.data.repository.BankingRepositoryImpl
import com.example.moviesdemoapp.feature.banking.domain.repository.BankingRepository
import com.example.moviesdemoapp.feature.banking.domain.usecase.GetBankingHomeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BankingModule {

    @Provides
    @Singleton
    fun provideBankingRepository(impl: BankingRepositoryImpl): BankingRepository = impl

    @Provides
    fun provideGetBankingHomeUseCase(repo: BankingRepository) = GetBankingHomeUseCase(repo)
}
