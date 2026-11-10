package com.example.competition.ui.profile

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.Award
import com.example.competition.domain.Participation
import com.example.competition.domain.Profile
import com.example.competition.domain.ProfileStats
import com.example.competition.ui.common.LoadableViewModel

/** 我的页数据：个人信息 + 成长统计 + 获奖 + 参赛经历。 */
data class ProfileData(
    val profile: Profile = Profile(),
    val stats: ProfileStats = ProfileStats(),
    val awards: List<Award> = emptyList(),
    val participations: List<Participation> = emptyList(),
)

/**
 * 我的页 ViewModel。
 *
 * 需要两个接口：
 * - `GET /profile`          → 个人信息（姓名/学校/专业/年级/兴趣）
 * - `GET /profile/overview` → 统计 + 获奖 + 参赛经历
 *
 * 两者并发拉取（`coroutineScope` + `async`），避免串行等待让首屏变慢。
 */
class ProfileViewModel(
    private val repository: CompetitionRepository,
) : LoadableViewModel<ProfileData>() {
    override val emptyValue: ProfileData = ProfileData()

    override suspend fun fetch(): ProfileData {
        val profile = repository.profile().getOrThrow()
        val overview = repository.profileOverview().getOrThrow()
        return ProfileData(
            profile = profile,
            stats = overview.stats,
            awards = overview.awards,
            participations = overview.participations,
        )
    }

    class Factory(
        repository: CompetitionRepository,
    ) : LoadableViewModel.Factory<ProfileViewModel>({ ProfileViewModel(repository) })
}