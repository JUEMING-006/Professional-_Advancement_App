package com.example.competition.ui.roles

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.Role
import com.example.competition.ui.common.LoadableViewModel

/**
 * 岗位选择页 ViewModel。
 *
 * 数据来源：`GET /roles?competitionId=...`。
 * 选好岗位后调 `POST /preparations` 建备赛项目 —— 这是「选岗位 → 学课时」链路的起点。
 */
class RoleSelectionViewModel(
    private val repository: CompetitionRepository,
    private val competitionId: String,
) : LoadableViewModel<List<Role>>() {
    override val emptyValue: List<Role> = emptyList()

    override suspend fun fetch(): List<Role> =
        repository.roles(competitionId).getOrThrow()

    /**
     * 创建备赛项目。
     *
     * @return 新建的备赛项目 id；失败返回 null（错误通过 [state] 暴露）
     */
    suspend fun createPreparation(
        roleId: String,
        teamName: String = "",
    ): String? =
        repository
            .createPreparation(competitionId = competitionId, roleId = roleId, teamName = teamName)
            .getOrNull()
            ?.id

    class Factory(
        private val repository: CompetitionRepository,
        private val competitionId: String,
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            RoleSelectionViewModel(repository, competitionId) as T
    }
}