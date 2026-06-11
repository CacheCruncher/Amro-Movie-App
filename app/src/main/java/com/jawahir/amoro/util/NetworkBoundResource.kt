package com.jawahir.amoro.util

import com.jawahir.amoro.domain.result.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

inline fun <T> networkBoundResource(
    crossinline fetchFromDb: () -> Flow<T>,
    crossinline shouldFetch: suspend (T) -> Boolean,
    crossinline fetchFromRemote: suspend () -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = {}
): Flow<NetworkResult<T>> = channelFlow {

    // Step1: emit from DB immediately
    val dbData = fetchFromDb().first()

    if (dbData != null) {
        send(NetworkResult.Success(dbData, false))
    }

    if (shouldFetch(dbData)) {
        val dbJob = launch{
            fetchFromDb().collect {
                send(NetworkResult.Success(it, isLoadingMore = true))
            }
        }

        // Step2: fetch from network and save to DB
        try {
            fetchFromRemote()
        } catch (e: Exception) {
            onFetchFailed(e)
            send(NetworkResult.NetworkError(e))
        }

        // fetch form remote done , cancel live collector
        dbJob.cancel()
    }

    // Step3: collect DB forever — emits on every DB write from fetch()
    fetchFromDb().collect {
        send(NetworkResult.Success(it, false))
    }
}