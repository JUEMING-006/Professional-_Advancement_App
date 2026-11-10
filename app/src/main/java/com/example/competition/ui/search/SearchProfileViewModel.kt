package com.example.competition.ui.search

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.CompetitionSummary
import com.example.competition.domain.Profile
import com.example.competition.ui.common.LoadableViewModel

/** 搜索画像数据：个人信息 + 候选赛事。 */
data class SearchProfileData(
    val profile: Profile = Profile(),
    val candidates: List<CompetitionSummary> = emptyList(),
)

/**
 * 搜索画像页 ViewModel。
 *
 * 数据来源：`GET /profile` + `GET /competitions`。
 * 「匹配理由」由前端基于兴趣方向与赛事赛道做**关键词匹配**，
 * 不是模型生成的 —— 这样可以解释、可复现。
 */
class SearchProfileViewModel(
    private val repository: CompetitionRepository,
) : LoadableViewModel<SearchProfileData>() {
    override val emptyValue: SearchProfileData = SearchProfileData()

    override suspend fun fetch(): SearchProfileData {
        val profile = repository.profile().getOrNull() ?: Profile()
        val items = repository.competitions().getOrNull()?.items.orEmpty()
        return SearchProfileData(profile = profile, candidates = items)
    }

    /**
     * 为某场比赛生成匹配理由。
     *
     * 规则：看用户的兴趣方向与赛事的赛道/标签是否有交集。
     * 没有交集时给中性提示，**不编造匹配度分数**（契约红线）。
     */
    fun matchReason(
        profile: Profile,
        competition: CompetitionSummary,
    ): String {
        val interests = profile.interestDirections.filter { it.isNotBlank() }
        if (interests.isEmpty()) {
            return "你还没有填写兴趣方向，去「我的」补充后可以更精准地匹配。"
        }
        val haystack = (listOf(competition.trackName, competition.categoryCode) +
            competition.tags).joinToString(" ")
        val hit = interests.filter { haystack.contains(it) }
        return if (hit.isNotEmpty()) {
            "你的兴趣方向「${hit.joinToString("、")}」与这场比赛的赛道相关。"
        } else {
            "这场比赛与你的兴趣方向暂无明显交集，可作为拓展尝试。"
        }
    }

    class Factory(
        repository: CompetitionRepository,
    ) : LoadableViewModel.Factory<SearchProfileViewModel>({
        SearchProfileViewModel(repository)
    })
}