package com.example.competition.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 页面加载状态。
 *
 * 所有数据页共用这一套状态语义，避免每个 ViewModel 各写一份
 * `loading/error/data` 三件套（原本 5 份，再写 9 屏会变 14 份重复）。
 */
sealed interface LoadState<out T> {
    /** 首次加载中，界面显示骨架/占位。 */
    data object Loading : LoadState<Nothing>

    /** 加载成功。`data` 可能为 null（如"没有备赛项目"也是成功结果）。 */
    data class Success<T>(val data: T) : LoadState<T>

    /** 加载失败。`message` 直接展示给用户，`retryable` 表示能否重试。 */
    data class Failure(
        val message: String,
        val retryable: Boolean = true,
    ) : LoadState<Nothing>
}

/**
 * 单次加载型 ViewModel 的基类。
 *
 * 把「首次进入才加载、失败可重试、协程作用域」这三件事收敛到一处。
 * 子类只需实现 [fetch] 与 [emptyValue]。
 *
 * @param T 业务数据类型
 */
abstract class LoadableViewModel<T> : ViewModel() {
    protected abstract val emptyValue: T

    private val _state = MutableStateFlow<LoadState<T>>(LoadState.Loading)
    val state: StateFlow<LoadState<T>> = _state.asStateFlow()

    private var started = false

    /** 首次进入时调用；已加载过则跳过，避免每次重组都发请求。 */
    fun ensureLoaded() {
        if (started) return
        started = true
        reload()
    }

    /** 强制重新加载（下拉刷新 / 重试按钮）。 */
    fun reload() {
        viewModelScope.launch {
            _state.value = LoadState.Loading
            runCatching { fetch() }
                .onSuccess { _state.value = LoadState.Success(it ?: emptyValue) }
                .onFailure { e ->
                    _state.value = LoadState.Failure(
                        message = e.message ?: "加载失败，请稍后重试",
                    )
                }
        }
    }

    /**
     * 取数据。返回 null 表示"成功但没有数据"（例如没有备赛项目），
     * 会被替换成 [emptyValue] 而不是当作失败。
     */
    protected abstract suspend fun fetch(): T?

    /** 通用工厂：所有子类共用，省掉每个 ViewModel 各写一遍 Factory。 */
    abstract class Factory<T : LoadableViewModel<*>>(
        private val create: () -> T,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
}