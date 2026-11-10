package com.example.competition.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.CompetitionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 发现页状态。
 *
 * 单一不可变状态对象：界面只读它，不持有自己的可变状态。
 */
data class DiscoveryUiState(
    val loading: Boolean = false,
    val items: List<CompetitionSummary> = emptyList(),
    val total: Int = 0,
    val errorMessage: String? = null,
    /** 数据来自缓存而非网络，用于提示"当前是离线数据"。 */
    val fromCache: Boolean = false,
)

class DiscoveryViewModel(
    private val repository: CompetitionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DiscoveryUiState())
    val state: StateFlow<DiscoveryUiState> = _state.asStateFlow()

    init {
        load()
    }

    /**
     * 是否已加载过。
     *
     * 从「添加比赛」返回时界面会重新组合，需要刷新才能看到新赛事；
     * 但如果每次重组都刷新，滚动或旋转屏幕会反复请求，所以由调用方
     * 在"确实需要刷新"的时机调 [refresh]。
     */
    private var loaded = false

    /** 强制重新拉取（从添加比赛返回、或用户下拉时用）。 */
    fun refresh() {
        load(force = true)
    }

    fun load(
        slot: String? = null,
        category: String? = null,
        keyword: String? = null,
        force: Boolean = false,
    ) {
        if (loaded && !force) return
        loaded = true

        _state.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.competitions(slot = slot, category = category, keyword = keyword)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            loading = false,
                            items = response.items,
                            total = response.total,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    // 把真实异常打到 logcat，便于定位连接/解析问题；
                    // 界面只展示简短提示，不暴露技术细节。
                    android.util.Log.e("DiscoveryViewModel", "加载赛事失败", error)
                    _state.update {
                        it.copy(
                            loading = false,
                            errorMessage = "加载失败：${error.javaClass.simpleName}",
                        )
                    }
                }
        }
    }

    class Factory(private val repository: CompetitionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DiscoveryViewModel(repository) as T
    }
}
