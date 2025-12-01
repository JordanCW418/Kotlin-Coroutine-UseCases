package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase4

import androidx.lifecycle.viewModelScope
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.MockApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class VariableAmountOfNetworkRequestsViewModel(
    private val mockApi: MockApi = mockApi()
) : BaseViewModel<UiState>() {

    fun performNetworkRequestsSequentially() {
        uiState.value = UiState.Loading
        viewModelScope.launch {
            val recentVersions = mockApi.getRecentAndroidVersions()
            val versionFeatures = recentVersions.map {
                mockApi.getAndroidVersionFeatures(it.apiLevel)
            }

            uiState.value = UiState.Success(versionFeatures)
        }
    }

    fun performNetworkRequestsConcurrently() {
        uiState.value = UiState.Loading

        val recentAndroidVersionsDeferred = viewModelScope.async {
            mockApi.getRecentAndroidVersions()
        }

        val androidVersionFeatures = viewModelScope.async {
            recentAndroidVersionsDeferred.await().map {
                viewModelScope.async {
                    mockApi.getAndroidVersionFeatures(it.apiLevel)
                }
            }
        }

        viewModelScope.launch {
            val versionFeatures = androidVersionFeatures.await().awaitAll()
            uiState.value = UiState.Success(versionFeatures)
        }
    }
}