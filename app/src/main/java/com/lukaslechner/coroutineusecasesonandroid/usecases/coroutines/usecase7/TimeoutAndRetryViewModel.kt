package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase7

import androidx.lifecycle.viewModelScope
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.MockApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class TimeoutAndRetryViewModel(
    private val api: MockApi = mockApi()
) : BaseViewModel<UiState>() {

    fun performNetworkRequest() {
        uiState.value = UiState.Loading
        val numberOfRetries = 2
        val timeout = 1000L

        val oreoVersionsDeferred = viewModelScope.async {
            retryWithTimeout(numberOfRetries, timeout) {
                api.getAndroidVersionFeatures(27)
            }
        }
        val pieVersionsDeferred = viewModelScope.async {
            retryWithTimeout(numberOfRetries, timeout) {
                api.getAndroidVersionFeatures(28)
            }
        }

        viewModelScope.launch {
            uiState.value = UiState.Success(awaitAll(oreoVersionsDeferred, pieVersionsDeferred))
        }

    }

    private suspend fun<T> retryWithTimeout(
        maxRetries: Int,
        timeoutMillis: Long,
        block: suspend () -> T): T = retry(maxRetries = maxRetries) {
              withTimeout(timeoutMillis) {
                    block()
              }
          }

    private suspend fun<T> retry(maxRetries: Int = 2,
                                block: suspend () -> T): T {
        repeat(maxRetries) {
            try {
                return block()
            }
            catch (exception: Exception) {
                Timber.e(exception)
            }
            delay(100)
        }
        return block()
    }
}