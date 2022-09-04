package com.android.application.financeapp.utils


sealed class ResultsOpt<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?): ResultsOpt<T>(data)
    class Error<T>(message: String, data: T? = null): ResultsOpt<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true): ResultsOpt<T>(null)
}
