package com.android.application.financeapp.domain.repository

import com.android.application.financeapp.domain.model.CompanyListing
import com.android.application.financeapp.utils.ResultsOpt
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<ResultsOpt<List<CompanyListing>>>
}
