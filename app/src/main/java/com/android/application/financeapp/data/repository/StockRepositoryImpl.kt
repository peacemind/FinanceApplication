package com.android.application.financeapp.data.repository

import com.android.application.financeapp.data.csv.CSVParser
import com.android.application.financeapp.data.csv.IntradayInfoParser
import com.opencsv.CSVReader
import com.android.application.financeapp.data.local.StockDatabase
import com.android.application.financeapp.data.mapper.toCompanyInfo
import com.android.application.financeapp.data.mapper.toCompanyListing
import com.android.application.financeapp.data.mapper.toCompanyListingEntity
import com.android.application.financeapp.data.remote.StockApi
import com.android.application.financeapp.domain.model.CompanyInfo
import com.android.application.financeapp.domain.model.CompanyListing
import com.android.application.financeapp.domain.model.IntradayInfo
import com.android.application.financeapp.domain.repository.StockRepository
import com.android.application.financeapp.utils.ResultsOpt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>,
): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<ResultsOpt<List<CompanyListing>>> {
        return flow {
            emit(ResultsOpt.Loading(true))
            val localListings = dao.searchCompanyListing(query)
            emit(ResultsOpt.Success(
                data = localListings.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if(shouldJustLoadFromCache) {
                emit(ResultsOpt.Loading(false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                companyListingsParser.parse(response.byteStream())
            } catch(e: IOException) {
                e.printStackTrace()
                emit(ResultsOpt.Error("Couldn't load data"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(ResultsOpt.Error("Couldn't load data"))
                null
            }

            remoteListings?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )
                emit(ResultsOpt.Success(
                    data = dao
                        .searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(ResultsOpt.Loading(false))
            }
        }
    }

    override suspend fun getIntradayInfo(symbol: String): ResultsOpt<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            ResultsOpt.Success(results)
        } catch(e: IOException) {
            e.printStackTrace()
            ResultsOpt.Error(
                message = "Couldn't load intraday info"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            ResultsOpt.Error(
                message = "Couldn't load intraday info"
            )
        }
    }

    override suspend fun getCompanyInfo(symbol: String): ResultsOpt<CompanyInfo> {
        return try {
            val result = api.getCompanyInfo(symbol)
            ResultsOpt.Success(result.toCompanyInfo())
        } catch(e: IOException) {
            e.printStackTrace()
            ResultsOpt.Error(
                message = "Couldn't load company info"
            )
        } catch(e: HttpException) {
            e.printStackTrace()
            ResultsOpt.Error(
                message = "Couldn't load company info"
            )
        }
    }
}
