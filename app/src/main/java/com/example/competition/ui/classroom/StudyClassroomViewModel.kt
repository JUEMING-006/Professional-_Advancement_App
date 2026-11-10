package com.example.competition.ui.classroom

import com.example.competition.data.CompetitionRepository
import com.example.competition.domain.NextLesson
import com.example.competition.domain.StudyPath
import com.example.competition.ui.common.LoadableViewModel

/** 学习课堂数据：路径全貌 + 当前该学的课。 */
data class ClassroomData(
    val path: StudyPath? = null,
    val current: NextLesson? = null,
)

/**
 * 学习课堂 ViewModel。
 *
 * 数据链路：`GET /preparations/{id}/study-path` + `GET /preparations/{id}/next-lesson`。
 * 下一课由后端**规则版**推荐（按 sort_order 找第一个未完成课时），不是模型猜的。
 */
class StudyClassroomViewModel(
    private val repository: CompetitionRepository,
    private val preparationId: String,
) : LoadableViewModel<ClassroomData>() {
    override val emptyValue: ClassroomData = ClassroomData()

    override suspend fun fetch(): ClassroomData {
        val path = repository.studyPath(preparationId).getOrNull()
        // 下一课取不到不算致命：路径仍可展示，只是没有「当前该学」。
        val next = repository.nextLesson(preparationId).getOrNull()
        return ClassroomData(path = path, current = next)
    }

    /** 完成当前课时；完成后重新拉取，让界面立刻反映新进度。 */
    suspend fun completeLesson(lessonId: String): Boolean {
        val ok = repository.completeLesson(lessonId).isSuccess
        if (ok) reload()
        return ok
    }

    class Factory(
        private val repository: CompetitionRepository,
        private val preparationId: String,
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            StudyClassroomViewModel(repository, preparationId) as T
    }
}