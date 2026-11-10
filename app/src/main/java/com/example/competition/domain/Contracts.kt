package com.example.competition.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * App↔后端共享的数据契约。
 *
 * 字段名、类型、可空性与后端 `com.cansaichi.server.domain` **逐字对应**，
 * 依据 `docs/03-架构与接口契约.md`。改这里必须同时改后端并补契约测试。
 *
 * 约定：
 * - 只放"从后端来的"数据；纯 UI 状态（高亮、进度比例、颜色）不进这里。
 * - 不用 `@SerialName`，除非后端字段名不是合法 Kotlin 标识符。
 */

// ===== 出处 =====

@Serializable
data class SourceRef(
    val label: String,
    val url: String = "",
    val docNo: String = "",
    val clauseNo: String = "",
    val fetchedAt: String = "",
)

// ===== 赛事 =====

@Serializable
data class CompetitionSummary(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val trackName: String = "",
    val categoryCode: String = "",
    val typeCode: String = "",
    val organizerName: String = "",
    val status: String,
    val deadline: String = "",
    val teamSizeText: String = "",
    val tags: List<String> = emptyList(),
    val isSample: Boolean,
    val isFavorite: Boolean,
    val source: SourceRef? = null,
)

@Serializable
data class CompetitionDetail(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val year: Int = 0,
    val status: String,
    val categoryName: String = "",
    val trackName: String = "",
    val eligibility: String = "",
    val format: String = "",
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val registrationDeadlineText: String = "",
    val minMembers: Int? = null,
    val maxMembers: Int? = null,
    val allowCrossSchool: Boolean? = null,
    val advisorCountMin: Int? = null,
    val advisorCountMax: Int? = null,
    val fee: Int? = null,
    val feeText: String = "",
    val organizer: String = "",
    val trackDescription: String = "",
    val trackTags: List<String> = emptyList(),
    val deliverables: List<String> = emptyList(),
    val awardsDescription: String = "",
    val noticeDocumentUrl: String = "",
    val isSample: Boolean,
    val source: SourceRef? = null,
    val fetchedAt: String = "",
)

@Serializable
data class CompetitionListResponse(
    val items: List<CompetitionSummary> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 0,
)

// ===== 评分 / 赛程 / 价值 =====

@Serializable
data class ScoringDimension(
    val id: String,
    val name: String,
    val score: Int,
    val description: String = "",
    val clauseNo: String = "",
    val quote: String = "",
    val source: SourceRef? = null,
)

@Serializable
data class ScoringResponse(
    val totalScore: Int = 0,
    val dimensions: List<ScoringDimension> = emptyList(),
)

@Serializable
data class ScheduleNode(
    val id: String,
    val date: String,
    val time: String = "",
    val title: String,
    val description: String = "",
    val kind: String = "",
    val isNext: Boolean = false,
)

@Serializable
data class ScheduleResponse(
    val timezone: String = "Asia/Shanghai",
    val nodes: List<ScheduleNode> = emptyList(),
)

@Serializable
data class ValueDimension(
    val name: String,
    val status: String,
    val description: String = "",
    val source: SourceRef? = null,
)

@Serializable
data class ValueChecklistItem(
    val id: String,
    val text: String,
)

@Serializable
data class ValueAssessmentResponse(
    val summary: String = "",
    val dimensions: List<ValueDimension> = emptyList(),
    val checklist: List<ValueChecklistItem> = emptyList(),
)

// ===== 岗位 =====

@Serializable
data class Role(
    val id: String,
    val name: String,
    val description: String = "",
    val learningFocus: String = "",
    val iconKey: String = "",
    val alignedScoringDimensionIds: List<String> = emptyList(),
)

// ===== 学习路径 =====

@Serializable
data class Lesson(
    val id: String,
    val label: String = "",
    val title: String,
    val durationMinutes: Int = 0,
    val type: String = "",
    val alignedScoringDimensionId: String = "",
    val deliverableText: String = "",
    val practicePrompt: String = "",
)

@Serializable
data class StudyModule(
    val id: String,
    val name: String,
    val description: String = "",
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val status: String = "not_started",
    val lessons: List<Lesson> = emptyList(),
)

@Serializable
data class StudyPath(
    val id: String,
    val pathName: String,
    val tagline: String = "",
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val weeklyHours: Int = 0,
    val targetDate: String = "",
    val modules: List<StudyModule> = emptyList(),
)

// ===== 备赛项目（个人赛与团队赛统一）=====

@Serializable
data class PreparationMember(
    val id: String,
    val displayName: String,
    val roleId: String = "",
    val joinedAt: String = "",
)

@Serializable
data class PreparationProgress(
    val completed: Int = 0,
    val total: Int = 0,
    val percent: Int = 0,
)

@Serializable
data class Preparation(
    val id: String,
    val competitionId: String,
    val competitionName: String = "",
    val trackName: String = "",
    val teamSizeText: String = "",
    val status: String,
    val keyDates: List<String> = emptyList(),
    val roleId: String = "",
    val roleName: String = "",
    val teamName: String = "",
    val projectIdea: String = "",
    val members: List<PreparationMember> = emptyList(),
    val progress: PreparationProgress? = null,
    val pathName: String = "",
)

@Serializable
data class ScoringRef(
    val dimensionId: String,
    val name: String,
    val score: Int,
)

@Serializable
data class NextLesson(
    val lessonId: String,
    val pathLabel: String = "",
    val moduleName: String = "",
    val lessonIndex: Int = 0,
    val title: String,
    val durationText: String = "",
    val alignedScoring: ScoringRef? = null,
    val deliverableText: String = "",
    val topicNote: String = "",
)

@Serializable
data class FollowUp(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
)

// ===== 记录（个人成长时间线）=====

@Serializable
data class DeliverableItem(
    val id: String,
    val text: String,
)

@Serializable
data class Deliverable(
    val id: String,
    val name: String,
    val version: String = "",
    val type: String = "text",
    val items: List<DeliverableItem> = emptyList(),
)

@Serializable
data class RelatedLesson(
    val moduleName: String = "",
    val lessonIndex: Int = 0,
)

@Serializable
data class Record(
    val id: String,
    val type: String,
    val title: String,
    val summary: String = "",
    val occurredAt: String,
    val durationMinutes: Int = 0,
    val deliverable: Deliverable? = null,
    val relatedLesson: RelatedLesson? = null,
    val projectId: String = "",
    val ownerId: String = "",
)

@Serializable
data class RecordStatistics(
    val totalRecords: Int = 0,
    val totalMinutes: Int = 0,
    val deliverableCount: Int = 0,
)

@Serializable
data class RecordGroup(
    val date: String,
    val count: Int = 0,
    val items: List<Record> = emptyList(),
)

@Serializable
data class RecordListResponse(
    val groups: List<RecordGroup> = emptyList(),
    val statistics: RecordStatistics = RecordStatistics(),
)

// ===== 我的 =====

@Serializable
data class Profile(
    val displayName: String = "",
    val avatarInitial: String = "",
    val school: String = "",
    val major: String = "",
    val grade: String = "",
    val interestDirections: List<String> = emptyList(),
)

@Serializable
data class Award(
    val id: String,
    val level: String = "",
    val name: String,
    val time: String = "",
    val meta: String = "",
)

@Serializable
data class Participation(
    val id: String,
    val name: String,
    val session: String = "",
    val period: String = "",
    val role: String = "",
    val result: String = "",
    val awarded: Boolean = false,
    val status: String = "",
)

@Serializable
data class ProfileStats(
    val competitions: Int = 0,
    val awards: Int = 0,
    val records: Int = 0,
)

@Serializable
data class ProfileOverview(
    val stats: ProfileStats = ProfileStats(),
    val awards: List<Award> = emptyList(),
    val participations: List<Participation> = emptyList(),
)

// ===== 分类与筛选 =====

@Serializable
data class Category(
    val code: String,
    val name: String,
    val title: String = "",
    val subtitle: String = "",
)

@Serializable
data class FilterOption(
    val code: String,
    val name: String,
)

@Serializable
data class CompetitionFilters(
    val types: List<FilterOption> = emptyList(),
    val organizers: List<FilterOption> = emptyList(),
    val sortOptions: List<FilterOption> = emptyList(),
)

// ===== 智能体抽取 =====

@Serializable
data class ExtractRequest(
    val text: String,
)

/**
 * 抽取结果。
 *
 * `valid = false` 是**正常业务结果**（模型抽不准），不是错误：
 * 此时 `competition` 为 null，但 `draft` 仍有内容，`errors` 说明原因。
 */
@Serializable
data class ExtractResponse(
    val valid: Boolean,
    val errors: List<String> = emptyList(),
    val competition: CompetitionDetail? = null,
    val draft: ExtractDraft,
    val notes: String = "",
)

@Serializable
data class ExtractDraft(
    val name: String = "",
    val organizer: String = "",
    val totalScore: Int = 0,
    val scoring: List<ScoringDimension> = emptyList(),
    val schedule: List<ScheduleNode> = emptyList(),
)

// ===== 赛事通知聚合（ADR-014） =====
//
// 边界：只存标题 + 链接 + 日期，**不存正文**。
// 用户点进去必须跳官网原文 —— 我们只做指路，不做转载。

@Serializable
data class NoticeDto(
    val id: String,
    val competitionKey: String = "",
    val competitionName: String = "",
    val title: String,
    /** 原文链接：用户必须跳到官网看正文。 */
    val url: String,
    val publishedDate: String = "",
    val sourceHost: String = "",
    val isRead: Boolean = false,
    val isImported: Boolean = false,
)

@Serializable
data class NoticeListResponse(
    val items: List<NoticeDto> = emptyList(),
    val total: Int = 0,
    val unreadCount: Int = 0,
)

@Serializable
data class NoticeSourceDto(
    val id: String,
    val competitionKey: String = "",
    val competitionName: String = "",
    val listUrl: String = "",
    val host: String = "",
    /** allowed / blocked / unknown —— 该站是否允许抓取。 */
    val compliance: String = "",
    val complianceNote: String = "",
    val enabled: Boolean = true,
    val lastFetchedAt: String = "",
    val lastStatus: String = "",
)
// ===== 创建备赛项目（E11） =====

@Serializable
data class CreatePreparationRequest(
    val competitionId: String,
    val roleId: String,
    val teamName: String = "",
    val projectIdea: String = "",
)