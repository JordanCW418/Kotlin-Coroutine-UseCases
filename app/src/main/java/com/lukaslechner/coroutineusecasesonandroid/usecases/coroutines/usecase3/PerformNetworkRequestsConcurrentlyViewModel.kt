package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase3

import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.MockApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class PerformNetworkRequestsConcurrentlyViewModel(
    private val mockApi: MockApi = mockApi()
) : BaseViewModel<UiState>() {

    fun performNetworkRequestsSequentially() {
        uiState.value = UiState.Loading
        viewModelScope.launch {
            try{
                val oreoFeatures = mockApi.getAndroidVersionFeatures(27)
                val pieFeatures = mockApi.getAndroidVersionFeatures(28)
                val android10Features = mockApi.getAndroidVersionFeatures(29)
                uiState.value = UiState.Success(mutableListOf(oreoFeatures, pieFeatures, android10Features))
            } catch(e: Exception) {
                UiState.Error("Network Failure")
            }
        }
    }

    fun performNetworkRequestsConcurrently() {
        uiState.value = UiState.Loading
        val deferred1 = viewModelScope.async {
            mockApi.getAndroidVersionFeatures(27)
        }
        val deferred2 = viewModelScope.async {
            mockApi.getAndroidVersionFeatures(28)
        }
        val deferred3 = viewModelScope.async {
            mockApi.getAndroidVersionFeatures(29)
        }

        viewModelScope.launch {
            try {
                val versionFeatures = awaitAll(deferred1, deferred2, deferred3)
                uiState.value = UiState.Success(versionFeatures)
            } catch (e: Exception) {
                uiState.value = UiState.Error("Network Failure")
            }
        }
    }
}