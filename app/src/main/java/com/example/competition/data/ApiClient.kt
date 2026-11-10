package com.example.competition.data

import android.util.Log
import com.example.competition.domain.CompetitionDetail
import com.example.competition.domain.CreatePreparationRequest
import com.example.competition.domain.CompetitionListResponse
import com.example.competition.domain.CompetitionSummary
import com.example.competition.domain.ExtractRequest
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 后端 API 客户端。
 *
 * 只做三件事：拼 URL、发请求、把 JSON 解成契约类。
 * 业务判断一律不在这里 —— 那是 Repository 的职责。
 */
class ApiClient(
    private val baseUrl: String,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        // 后端新增字段时不至于让 App 崩 —— 但这本身是契约破坏性变更，
        // 契约 §6 规则 3 要求两边同步。
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .get()
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw ApiException(response.code, "请求失败：$path（HTTP ${response.code}）")
            }
            response.body?.string().orEmpty()
        }
    }

    private suspend fun post(path: String, body: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .post(body.toRequestBody(JSON_MEDIA_TYPE))
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw ApiException(response.code, "请求失败：$path（HTTP ${response.code}）")
            }
            response.body?.string().orEmpty()
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE: MediaType = "application/json; charset=utf-8".toMediaType()
    }

    private inline fun <reified T> decode(raw: String): T = json.decodeFromString(raw)

    // ===== 赛事 =====

    suspend fun competitions(
        slot: String? = null,
        category: String? = null,
        type: String? = null,
        organizer: String? = null,
        keyword: String? = null,
        sort: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): CompetitionListResponse {
        val query = buildList {
            slot?.let { add("slot=$it") }
            category?.let { add("category=$it") }
            type?.let { add("type=$it") }
            organizer?.let { add("organizer=$it") }
            keyword?.let { add("keyword=${it.encode()}") }
            sort?.let { add("sort=$it") }
            add("page=$page")
            add("size=$size")
        }.joinToString("&")
        return decode(get("/api/v1/competitions?$query"))
    }

    suspend fun competitionDetail(id: String): CompetitionDetail =
        decode(get("/api/v1/competitions/$id"))

    suspend fun scoring(id: String): ScoringResponse =
        decode(get("/api/v1/competitions/$id/scoring"))

    suspend fun schedule(id: String): ScheduleResponse =
        decode(get("/api/v1/competitions/$id/schedule"))

    suspend fun valueAssessment(id: String, roleId: String? = null): ValueAssessmentResponse {
        val suffix = roleId?.let { "?roleId=$it" } ?: ""
        return decode(get("/api/v1/competitions/$id/value-assessment$suffix"))
    }

    // ===== 岗位与学习路径 =====

    suspend fun roles(competitionId: String, categoryCode: String? = null): List<Role> {
        val suffix = categoryCode?.let { "&categoryCode=$it" } ?: ""
        return decode(get("/api/v1/roles?competitionId=$competitionId$suffix"))
    }

    suspend fun studyPath(preparationId: String, roleId: String? = null): StudyPath {
        val suffix = roleId?.let { "?roleId=$it" } ?: ""
        return decode(get("/api/v1/preparations/$preparationId/study-path$suffix"))
    }

    suspend fun createPreparation(request: CreatePreparationRequest): Preparation =
        decode(post("/api/v1/preparations", json.encodeToString(request)))
    suspend fun preparations(): List<Preparation> =
        decode(get("/api/v1/preparations"))

    suspend fun completeLesson(lessonId: String) {
        post("/api/v1/lessons/$lessonId/complete", "{}")
    }
    suspend fun nextLesson(preparationId: String): NextLesson =
        decode(get("/api/v1/preparations/$preparationId/next-lesson"))

    // ===== 记录 =====

    suspend fun records(
        type: String? = null,
        projectId: String? = null,
        from: String? = null,
        to: String? = null,
        page: Int = 0,
        size: Int = 50,
    ): RecordListResponse {
        val query = buildList {
            type?.let { add("type=$it") }
            projectId?.let { add("projectId=$it") }
            from?.let { add("from=$it") }
            to?.let { add("to=$it") }
            add("page=$page")
            add("size=$size")
        }.joinToString("&")
        return decode(get("/api/v1/records?$query"))
    }

    suspend fun record(id: String): Record = decode(get("/api/v1/records/$id"))

    // ===== 智能体抽取 =====

    /**
     * 把章程文本交给后端智能体抽取。
     *
     * 返回的 [ExtractResponse] 可能 `valid=false` —— 那是**正常业务结果**
     * （模型抽得不准），不是错误，App 应展示错误清单让用户判断。
     */
    suspend fun extract(text: String): ExtractResponse {
        val payload = json.encodeToString(ExtractRequest(text))
        return decode(post("/api/v1/extract", payload))
    }

    // ===== 我的 =====

    suspend fun notices(competitionKey: String? = null, limit: Int = 100): NoticeListResponse {
        val query = buildString {
            append("/api/v1/notices?limit=").append(limit)
            if (!competitionKey.isNullOrBlank()) append("&competitionKey=").append(competitionKey)
        }
        return decode(get(query))
    }
    suspend fun profile(): Profile = decode(get("/api/v1/me/profile"))

    suspend fun profileOverview(): ProfileOverview = decode(get("/api/v1/me/overview"))
}

/** API 调用失败。message 面向用户，code 面向排查。 */
class ApiException(
    val code: Int,
    override val message: String,
) : IOException(message)

/** URL 查询参数编码。 */
private fun String.encode(): String =
    java.net.URLEncoder.encode(this, Charsets.UTF_8.name())
