package com.android.application.financeapp.presentation.company_info

import com.android.application.financeapp.domain.model.CompanyInfo
import com.android.application.financeapp.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockInfos: List<IntradayInfo> = emptyList(),
    val company: CompanyInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
