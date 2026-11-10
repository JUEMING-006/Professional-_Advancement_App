package com.example.competition.ui.hot

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.CompetitionSummary
import com.example.competition.ui.common.LoadableViewModel

/** 分类定义（纯展示，不来自后端）。 */
data class HotCategory(
    val name: String,
    val title: String,
    val subtitle: String,
    /** 对应后端的 `categoryCode`；空串表示「全部」。 */
    val code: String,
)

/**
 * 热门与专业赛事页 ViewModel。
 *
 * 数据来源：`GET /competitions`（可按 category 过滤）。
 * 分类本身是前端展示结构，赛事数据全部来自后端。
 */
class HotProfessionalViewModel(
    private val repository: CompetitionRepository,
) : LoadableViewModel<List<CompetitionSummary>>() {
    override val emptyValue: List<CompetitionSummary> = emptyList()

    override suspend fun fetch(): List<CompetitionSummary> =
        repository.competitions().getOrThrow().items

    /** 按分类过滤（本地过滤，避免每切一次分类就发一次请求）。 */
    fun filterByCategory(
        items: List<CompetitionSummary>,
        code: String,
    ): List<CompetitionSummary> =
        if (code.isBlank()) items else items.filter { it.categoryCode == code }

    class Factory(
        repository: CompetitionRepository,
    ) : LoadableViewModel.Factory<HotProfessionalViewModel>({
        HotProfessionalViewModel(repository)
    })
}