package com.example.competition.data

import com.example.competition.domain.CompetitionDetail
import com.example.competition.domain.CreatePreparationRequest
import com.example.competition.domain.CompetitionListResponse
import com.example.competition.domain.CompetitionSummary
import com.example.competition.domain.ExtractResponse
import com.example.competition.domain.NextLesson
import com.example.competition.domain.Preparation
import com.example.competition.domain.NoticeListResponse
import com.example.competition.domain.Profile
import com.example.competition.domain.ProfileOverview
import com.example.competition.domain.Record
import com.example.competition.domain.RecordListResponse
import com.example.competition.domain.Role
import com.example.competition.domain.ScheduleResponse
import com.example.competition.domain.ScoringResponse
import com.example.competition.domain.StudyPath
import com.example.competition.domain.ValueAssessmentResponse

/**
 * 数据仓库。
 *
 * 职责：调 API、**缓存结果**、把异常翻译成可展示的状态。
 * 界面只问 Repository 要数据，不直接碰 ApiClient。
 *
 * 缓存的意义：后端不可用时仍能展示上一次的数据（离线兜底）。
 */
class CompetitionRepository(
    private val api: ApiClient,
) {
    // ===== 赛事 =====

    private var competitionCache: CompetitionListResponse? = null
    private val detailCache = mutableMapOf<String, CompetitionDetail>()

    suspend fun competitions(
        slot: String? = null,
        category: String? = null,
        type: String? = null,
        organizer: String? = null,
        keyword: String? = null,
        sort: String? = null,
    ): Result<CompetitionListResponse> = call {
        api.competitions(slot, category, type, organizer, keyword, sort)
            .also { competitionCache = it }
    }.recoverWithCache { competitionCache }

    suspend fun competitionDetail(id: String): Result<CompetitionDetail> = call {
        api.competitionDetail(id).also { detailCache[id] = it }
    }.recoverWithCache { detailCache[id] }

    suspend fun scoring(id: String): Result<ScoringResponse> = call { api.scoring(id) }

    suspend fun schedule(id: String): Result<ScheduleResponse> = call { api.schedule(id) }

    suspend fun valueAssessment(id: String, roleId: String? = null): Result<ValueAssessmentResponse> =
        call { api.valueAssessment(id, roleId) }

    // ===== 岗位与学习路径 =====

    suspend fun roles(competitionId: String, categoryCode: String? = null): Result<List<Role>> =
        call { api.roles(competitionId, categoryCode) }

    suspend fun studyPath(preparationId: String, roleId: String? = null): Result<StudyPath> =
        call { api.studyPath(preparationId, roleId) }

    /**
     * 创建备赛项目（选岗位后的下一步）。
     *
     * 后端会复制一份岗位默认学习路径作为项目专属路径，
     * 所以不同项目的进度互不影响。
     */
    suspend fun createPreparation(
        competitionId: String,
        roleId: String,
        teamName: String = "",
        projectIdea: String = "",
    ): Result<Preparation> = call {
        api.createPreparation(
            CreatePreparationRequest(
                competitionId = competitionId,
                roleId = roleId,
                teamName = teamName,
                projectIdea = projectIdea,
            ),
        )
    }
    suspend fun preparations(): Result<List<Preparation>> = call { api.preparations() }

    /** 完成课时（E27）；后端会向上汇总模块与路径进度。 */
    suspend fun completeLesson(lessonId: String): Result<Unit> = call {
        api.completeLesson(lessonId)
    }
    suspend fun nextLesson(preparationId: String): Result<NextLesson> =
        call { api.nextLesson(preparationId) }

    // ===== 记录 =====

    suspend fun records(
        type: String? = null,
        projectId: String? = null,
    ): Result<RecordListResponse> = call { api.records(type = type, projectId = projectId) }

    suspend fun record(id: String): Result<Record> = call { api.record(id) }

    // ===== 我的 =====

    /** 赛事通知（ADR-014）：只含标题 + 链接 + 日期，正文需跳官网。 */
    suspend fun notices(
        competitionKey: String? = null,
        limit: Int = 100,
    ): Result<NoticeListResponse> = call { api.notices(competitionKey, limit) }
    suspend fun profile(): Result<Profile> = call { api.profile() }

    suspend fun profileOverview(): Result<ProfileOverview> = call { api.profileOverview() }

    // ===== 智能体抽取 =====

    /**
     * 把章程原文交给后端智能体抽取。
     *
     * 注意：`valid = false` 走 **success 分支** —— 那是业务结果不是异常。
     */
    suspend fun extract(text: String): Result<ExtractResponse> = call { api.extract(text) }
}

/**
 * 执行一次 API 调用，把异常转成 [Result]。
 *
 * 用 [Result] 而不是抛异常，是为了让界面能显式区分"加载中/失败/成功"三态，
 * 而不是靠 try-catch 散落各处。
 */
private suspend inline fun <T> call(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }

/**
 * 失败时回退到缓存值。
 *
 * 后端不可用时用旧数据兜底，比直接空白页好 —— 但只在缓存非空时回退。
 */
private inline fun <T> Result<T>.recoverWithCache(
    crossinline cache: () -> T?,
): Result<T> = fold(
    onSuccess = { Result.success(it) },
    onFailure = { error ->
        val cached = cache()
        if (cached != null) Result.success(cached) else Result.failure(error)
    },
)
