package com.example.competition.ui.records

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.RecordListResponse
import com.example.competition.ui.common.LoadableViewModel

/**
 * 记录页 ViewModel。
 *
 * 只声明「取什么数据」，加载状态与重试逻辑由 [LoadableViewModel] 统一处理。
 */
class RecordListViewModel(
    private val repository: CompetitionRepository,
) : LoadableViewModel<RecordListResponse>() {
    override val emptyValue: RecordListResponse = RecordListResponse()

    override suspend fun fetch(): RecordListResponse? =
        repository.records().getOrThrow()

    /** 通用工厂的薄封装，调用处写法保持不变。 */
    class Factory(
        repository: CompetitionRepository,
    ) : LoadableViewModel.Factory<RecordListViewModel>({ RecordListViewModel(repository) })
}