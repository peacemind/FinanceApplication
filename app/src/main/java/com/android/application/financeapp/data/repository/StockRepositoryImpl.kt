package com.android.application.financeapp.data.repository

import com.android.application.financeapp.data.csv.CSVParser
import com.opencsv.CSVReader
import com.android.application.financeapp.data.local.StockDatabase
import com.android.application.financeapp.data.mapper.toCompanyListing
import com.android.application.financeapp.data.mapper.toCompanyListingEntity
import com.android.application.financeapp.data.remote.StockApi
import com.android.application.financeapp.domain.model.CompanyListing
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
    private val companyListingsParser: CSVParser<CompanyListing>
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
}
