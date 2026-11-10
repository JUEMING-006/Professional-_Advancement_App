package com.example.competition.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.CompetitionDetail
import com.example.competition.domain.ScheduleResponse
import com.example.competition.domain.ScoringResponse
import com.example.competition.domain.ValueAssessmentResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 赛事详情的三合一状态。
 *
 * 详情、评分细则、赛程、价值评估都按 `competitionId` 取，
 * 放在一个 ViewModel 里共享，避免同一份赛事在多屏重复拉取。
 */
data class CompetitionDetailUiState(
    val loading: Boolean = false,
    val detail: CompetitionDetail? = null,
    val scoring: ScoringResponse? = null,
    val schedule: ScheduleResponse? = null,
    val valueAssessment: ValueAssessmentResponse? = null,
    val errorMessage: String? = null,
)

class CompetitionDetailViewModel(
    private val repository: CompetitionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CompetitionDetailUiState())
    val state: StateFlow<CompetitionDetailUiState> = _state.asStateFlow()

    private var loadedId: String? = null

    fun load(competitionId: String, force: Boolean = false) {
        if (!force && loadedId == competitionId) return
        loadedId = competitionId

        _state.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            // 四个请求**并发**发出，而不是串行等待。
            // 串行时模拟器软渲染下总耗时 7+ 秒，直接触发 ANR（已实测）。
            val detailDeferred = async { repository.competitionDetail(competitionId) }
            val scoringDeferred = async { repository.scoring(competitionId) }
            val scheduleDeferred = async { repository.schedule(competitionId) }
            val valueDeferred = async { repository.valueAssessment(competitionId) }

            val detailResult = detailDeferred.await()
            val scoringResult = scoringDeferred.await()
            val scheduleResult = scheduleDeferred.await()
            val valueResult = valueDeferred.await()

            val error = listOf(detailResult, scoringResult, scheduleResult, valueResult)
                .firstNotNullOfOrNull { it.exceptionOrNull() }

            _state.update {
                it.copy(
                    loading = false,
                    detail = detailResult.getOrNull() ?: it.detail,
                    scoring = scoringResult.getOrNull() ?: it.scoring,
                    schedule = scheduleResult.getOrNull() ?: it.schedule,
                    valueAssessment = valueResult.getOrNull() ?: it.valueAssessment,
                    errorMessage = error?.message,
                )
            }
        }
    }

    class Factory(private val repository: CompetitionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CompetitionDetailViewModel(repository) as T
    }
}
