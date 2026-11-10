package com.example.competition.ui.preparation

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.NextLesson
import com.example.competition.domain.Preparation
import com.example.competition.ui.common.LoadableViewModel

/** 备赛工作台数据：项目本身 + 下一课推荐。 */
data class PreparationData(
    val preparation: Preparation? = null,
    val nextLesson: NextLesson? = null,
)

/**
 * 备赛工作台 ViewModel。
 *
 * 数据链路：`GET /preparations` 取当前项目 → `GET /{id}/next-lesson` 取下一课。
 * 下一课由后端**规则版**推荐（按 sort_order 找第一个未完成课时），不是模型猜的。
 */
class PreparationViewModel(
    private val repository: CompetitionRepository,
) : LoadableViewModel<PreparationData>() {
    override val emptyValue: PreparationData = PreparationData()

    override suspend fun fetch(): PreparationData? {
        val current = repository.preparations().getOrThrow().firstOrNull()
            ?: return PreparationData()

        // 下一课取不到不算致命：仍展示项目信息，只是没有推荐。
        val lesson = repository.nextLesson(current.id).getOrNull()
        return PreparationData(preparation = current, nextLesson = lesson)
    }

    /** 通用工厂的薄封装。 */
    class Factory(
        repository: CompetitionRepository,
    ) : LoadableViewModel.Factory<PreparationViewModel>({ PreparationViewModel(repository) })
}