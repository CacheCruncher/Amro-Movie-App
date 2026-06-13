package com.jawahir.amoro.util

import com.jawahir.amoro.domain.result.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

inline fun <T> networkBoundResource(
    crossinline fetchFromDb: () -> Flow<T>,
    crossinline isEmpty: (T) -> Boolean,
    crossinline shouldFetch: suspend () -> Boolean,
    crossinline fetchFromRemote: suspend () -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = {}
): Flow<NetworkResult<T>> = channelFlow {

    // Step1: fetch from DB
    val dbData = fetchFromDb().first()
    val fetchNeeded = shouldFetch()

    // Emit from DB only if there is actual data
    val hasData = !isEmpty(dbData)

    if (hasData) {
        send(NetworkResult.Success(dbData, fetchNeeded))
    }

    if (fetchNeeded) {
        // live updates while fetching (progressive — useful for paginated lists)
        val dbJob = launch {
            fetchFromDb().collect {
                if (!isEmpty(it)) {
                    send(NetworkResult.Success(it, isLoadingMore = true))
                }
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

        val finalDbData = fetchFromDb().first()
        send(NetworkResult.Success(finalDbData, isLoadingMore = false))
    }
}