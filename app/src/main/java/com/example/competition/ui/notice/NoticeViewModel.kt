package com.example.competition.ui.notice

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.NoticeListResponse
import com.example.competition.ui.common.LoadableViewModel

/**
 * 赛事通知 ViewModel（ADR-014）。
 *
 * 数据来源：`GET /api/v1/notices`。
 * 通知由后端定时抓取各赛事官网的**列表页**得到，只含标题 + 链接 + 日期。
 */
class NoticeViewModel(
    private val repository: CompetitionRepository,
) : LoadableViewModel<NoticeListResponse>() {
    override val emptyValue: NoticeListResponse = NoticeListResponse()

    override suspend fun fetch(): NoticeListResponse =
        repository.notices(limit = 100).getOrThrow()

    class Factory(
        repository: CompetitionRepository,
    ) : LoadableViewModel.Factory<NoticeViewModel>({ NoticeViewModel(repository) })
}