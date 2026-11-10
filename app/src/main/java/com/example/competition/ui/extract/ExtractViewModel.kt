package com.example.competition.ui.extract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.ExtractResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 「添加比赛」界面状态。
 *
 * 三步：输入章程文本 → 智能体抽取 → 展示结果（成功或错误清单）。
 */
data class ExtractUiState(
    val text: String = "",
    val extracting: Boolean = false,
    val result: ExtractResponse? = null,
    val errorMessage: String? = null,
) {
    /** 输入非空才能提交，避免用户点空。 */
    val canSubmit: Boolean get() = text.isNotBlank() && !extracting
}

class ExtractViewModel(
    private val repository: CompetitionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExtractUiState())
    val state: StateFlow<ExtractUiState> = _state.asStateFlow()

    fun onTextChange(value: String) {
        _state.update { it.copy(text = value, errorMessage = null) }
    }

    /** 提交抽取。 */
    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(extracting = true, errorMessage = null, result = null) }
        viewModelScope.launch {
            repository.extract(current.text)
                .onSuccess { response ->
                    // valid=false 也是成功返回：让用户看到"抽到什么"和"哪里不对"。
                    _state.update { it.copy(extracting = false, result = response) }
                }
                .onFailure { error ->
                    android.util.Log.e("ExtractViewModel", "抽取请求失败", error)
                    _state.update {
                        it.copy(
                            extracting = false,
                            errorMessage = "请求失败：${error.message ?: error.javaClass.simpleName}",
                        )
                    }
                }
        }
    }

    fun reset() {
        _state.value = ExtractUiState()
    }

    class Factory(private val repository: CompetitionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExtractViewModel(repository) as T
    }
}
