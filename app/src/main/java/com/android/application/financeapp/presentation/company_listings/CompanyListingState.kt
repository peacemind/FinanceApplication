package com.android.application.financeapp.presentation.company_listings

import com.android.application.financeapp.domain.model.CompanyListing


data class CompanyListingsState(
    val companies: List<CompanyListing> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = ""
)
