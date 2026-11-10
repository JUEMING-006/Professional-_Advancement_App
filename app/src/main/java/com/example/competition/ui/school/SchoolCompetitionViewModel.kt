package com.example.competition.ui.school

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.CompetitionSummary
import com.example.competition.ui.common.LoadableViewModel

/**
 * 校赛页 ViewModel。
 *
 * 数据来源：`GET /competitions?slot=school`。
 * 校赛是「按槽位筛选」的赛事，不是独立数据源 —— 与发现页共用同一接口。
 */
class SchoolCompetitionViewModel(
    private val repository: CompetitionRepository,
) : LoadableViewModel<List<CompetitionSummary>>() {
    override val emptyValue: List<CompetitionSummary> = emptyList()

    override suspend fun fetch(): List<CompetitionSummary> =
        repository.competitions(slot = "school").getOrThrow().items

    class Factory(
        repository: CompetitionRepository,
    ) : LoadableViewModel.Factory<SchoolCompetitionViewModel>({
        SchoolCompetitionViewModel(repository)
    })
}