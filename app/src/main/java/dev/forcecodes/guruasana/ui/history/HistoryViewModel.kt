package dev.forcecodes.guruasana.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.guruasana.data.db.PostProcessMetricsDao
import dev.forcecodes.guruasana.logger.Logger
import dev.forcecodes.guruasana.model.PostProcessMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val postProcessMetrics: PostProcessMetricsDao
) : ViewModel() {

    private val _processMetrics = MutableStateFlow(emptyList<PostProcessMetrics>())
    val processMetrics = _processMetrics.asStateFlow()

    fun saveClassificationResults(metrics: PostProcessMetrics) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Logger.d("Saving...")
                postProcessMetrics.updatePoses(metrics)
            }
        }
    }

    init {
        viewModelScope.launch {
            postProcessMetrics.getAllMetrics().collect {
                _processMetrics.value = it
            }
        }
    }
}