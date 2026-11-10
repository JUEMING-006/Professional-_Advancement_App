package com.example.competition.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.Record
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 记录详情状态。 */
data class RecordDetailUiState(
    val loading: Boolean = false,
    val record: Record? = null,
    val errorMessage: String? = null,
)

/**
 * 记录详情 ViewModel。
 *
 * 与列表页不同，这里必须**按 id 拉取**，不能用列表缓存 ——
 * 详情含成果条目等列表不返回的字段。
 */
class RecordDetailViewModel(
    private val repository: CompetitionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(RecordDetailUiState())
    val state: StateFlow<RecordDetailUiState> = _state.asStateFlow()

    private var loadedId: String? = null

    fun load(recordId: String, force: Boolean = false) {
        if (!force && loadedId == recordId) return
        loadedId = recordId

        _state.value = RecordDetailUiState(loading = true)
        viewModelScope.launch {
            repository
                .record(recordId)
                .onSuccess { record ->
                    _state.value = RecordDetailUiState(loading = false, record = record)
                }.onFailure { e ->
                    _state.value = RecordDetailUiState(
                        loading = false,
                        errorMessage = e.message ?: "记录加载失败",
                    )
                }
        }
    }

    class Factory(
        private val repository: CompetitionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RecordDetailViewModel(repository) as T
    }
}