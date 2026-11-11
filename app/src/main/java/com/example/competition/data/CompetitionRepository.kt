package com.example.competition.data

import com.example.competition.domain.CompetitionDetail
import com.example.competition.domain.CompetitionListResponse
import com.example.competition.domain.ExtractResponse
import com.example.competition.domain.NextLesson
import com.example.competition.domain.NoticeListResponse
import com.example.competition.domain.Preparation
import com.example.competition.domain.Profile
import com.example.competition.domain.ProfileOverview
import com.example.competition.domain.Record
import com.example.competition.domain.RecordListResponse
import com.example.competition.domain.Role
import com.example.competition.domain.ScheduleResponse
import com.example.competition.domain.ScoringResponse
import com.example.competition.domain.StudyPath
import com.example.competition.domain.ValueAssessmentResponse
import kotlinx.coroutines.delay

/**
 * 数据仓库（演示版，离线）。
 *
 * 后端不在本仓库，本类**不发起任何网络请求**，所有数据来自 [DemoData]。
 * 方法签名与原网络版保持一致，因此 14 个页面的 ViewModel 与 Factory 无需改动。
 *
 * 每个方法都带一小段 [delay]，用于模拟网络加载，让 Loading/骨架屏状态可见，
 * 也让界面切换有自然的节奏。返回类型统一为 [Result]，与原有契约一致。
 */
class CompetitionRepository {

    /** 包裹一段演示数据为 [Result]，并模拟加载耗时。 */
    private suspend fun <T> demo(block: () -> T): Result<T> {
        delay(DEMO_DELAY_MS)
        return Result.success(block())
    }

    // ===== 赛事 =====

    suspend fun competitions(
        slot: String? = null,
        category: String? = null,
        type: String? = null,
        organizer: String? = null,
        keyword: String? = null,
        sort: String? = null,
    ): Result<CompetitionListResponse> = demo {
        var items = when (slot) {
            // 校赛页只看校级/院级赛事，与「按槽位筛选」语义一致。
            "school" -> DemoData.competitions.filter { it.typeCode in SCHOOL_TYPES }
            else -> DemoData.competitions
        }
        if (!category.isNullOrBlank()) {
            items = items.filter { it.categoryCode == category }
        }
        if (!keyword.isNullOrBlank()) {
            items = items.filter {
                it.name.contains(keyword) || it.tags.any { tag -> tag.contains(keyword) }
            }
        }
        CompetitionListResponse(items = items, total = items.size, page = 0, size = items.size)
    }

    suspend fun competitionDetail(id: String): Result<CompetitionDetail> = demo {
        DemoData.details[id] ?: DemoData.details.values.first()
    }

    suspend fun scoring(id: String): Result<ScoringResponse> = demo { DemoData.scoring }

    suspend fun schedule(id: String): Result<ScheduleResponse> = demo { DemoData.schedule }

    suspend fun valueAssessment(id: String, roleId: String? = null): Result<ValueAssessmentResponse> =
        demo { DemoData.valueAssessment }

    // ===== 岗位与学习路径 =====

    suspend fun roles(competitionId: String, categoryCode: String? = null): Result<List<Role>> =
        demo { DemoData.roles }

    suspend fun studyPath(preparationId: String, roleId: String? = null): Result<StudyPath> =
        demo { DemoData.studyPath }

    suspend fun createPreparation(
        competitionId: String,
        roleId: String,
        teamName: String = "",
        projectIdea: String = "",
    ): Result<Preparation> = demo { DemoData.preparations.first() }

    suspend fun preparations(): Result<List<Preparation>> = demo { DemoData.preparations }

    suspend fun completeLesson(lessonId: String): Result<Unit> = demo { Unit }

    suspend fun nextLesson(preparationId: String): Result<NextLesson> = demo { DemoData.nextLesson }

    // ===== 记录 =====

    suspend fun records(
        type: String? = null,
        projectId: String? = null,
    ): Result<RecordListResponse> = demo { DemoData.records }

    suspend fun record(id: String): Result<Record> = demo {
        DemoData.records.groups.flatMap { it.items }.firstOrNull { it.id == id }
            ?: DemoData.records.groups.first().items.first()
    }

    // ===== 我的 =====

    suspend fun notices(
        competitionKey: String? = null,
        limit: Int = 100,
    ): Result<NoticeListResponse> = demo { DemoData.notices }

    suspend fun profile(): Result<Profile> = demo { DemoData.profile }

    suspend fun profileOverview(): Result<ProfileOverview> = demo { DemoData.profileOverview }

    // ===== 智能体抽取 =====

    suspend fun extract(text: String): Result<ExtractResponse> = demo { DemoData.extractResult }

    private companion object {
        const val DEMO_DELAY_MS = 250L

        /** 校赛槽位对应的赛事级别码（校级/院级）。 */
        val SCHOOL_TYPES = setOf("campus", "school", "college")
    }
}
