package com.android.application.financeapp.di

import com.android.application.financeapp.data.csv.CSVParser
import com.android.application.financeapp.data.csv.CompanyListingsParser
import com.android.application.financeapp.data.csv.IntradayInfoParser
import com.android.application.financeapp.data.repository.StockRepositoryImpl
import com.android.application.financeapp.domain.model.CompanyListing
import com.android.application.financeapp.domain.model.IntradayInfo
import com.android.application.financeapp.domain.repository.StockRepository
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
    abstract fun bindCompanyListingsParser(
        companyListingsParser: CompanyListingsParser
    ): CSVParser<CompanyListing>

    @Binds
    @Singleton
    abstract fun bindIntradayInfoParser(
        intradayInfoParser: IntradayInfoParser
    ): CSVParser<IntradayInfo>

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ): StockRepository
}