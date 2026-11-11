package com.example.competition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.competition.ui.components.AppTab
import com.example.competition.ui.components.LiquidGlassBottomBar
import com.example.competition.ui.theme.CompetitionTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompetitionTheme {
                CompetitionApp()
            }
        }
    }
}

/**
 * 应用根。
 *
 * 导航从"无参枚举"改为**带参路由** —— 原型里的 `AppScreen` 13 个枚举全无参，
 * 详情页因此不知道该显示哪条数据。带参是接后端的前提。
 *
 * 底栏为液态玻璃，且是内容层的**兄弟节点**：玻璃要折射背景，
 * 必须先记录内容层，玻璃自身不能被记录进去。
 */

/** 玻璃底栏占用的高度：内容层要留出这么多底部空间，否则页面自己的按钮会被盖住。 */
private val BOTTOM_BAR_HEIGHT = 88.dp

@Composable
private fun CompetitionApp() {
    // 用返回栈替代单一路由：navigate 压栈、goBack 弹栈、resetTo（底部 Tab 切换）清空重建。
    // 显式指定泛型为 AppRoute，否则会被推断成 AppRoute.Discovery 而无法压入其它路由。
    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Discovery) }
    val route = backStack.last()
    val backdrop = rememberLayerBackdrop()

    fun navigate(target: AppRoute) {
        backStack.add(target)
    }

    fun goBack() {
        // 至少保留根路由，避免弹空后无页面可显示。
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun resetTo(target: AppRoute) {
        backStack.clear()
        backStack.add(target)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 内容层：先记录，供玻璃折射采样。
        // ⚠️ 内容层底部留出玻璃底栏的高度：
        // 底栏是**浮在上面的兄弟节点**（为了做玻璃折射，不能放进内容层），
        // 不给内容层留边距的话，页面自己的底部按钮会被底栏盖住（已实测）。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = BOTTOM_BAR_HEIGHT)
                .graphicsLayer { alpha = 0.99f }
                .layerBackdrop(backdrop),
        ) {
            when (val current = route) {
                is AppRoute.Discovery -> CompetitionDiscoveryScreen(
                    onHotCompetitionClick = { navigate(AppRoute.HotProfessional) },
                    onSearchClick = { navigate(AppRoute.SearchProfile) },
                    onCategoryClick = { navigate(AppRoute.ProfessionalCategory) },
                    onDeadlineClick = { navigate(AppRoute.RecentDeadline) },
                    onDetailClick = { id -> navigate(AppRoute.CompetitionDetail(id)) },
                    onAddCompetitionClick = { navigate(AppRoute.AddCompetition) },
                onNoticeClick = { navigate(AppRoute.Notice) },
                    onSchoolCompetitionClick = { resetTo(AppRoute.SchoolCompetition) },
                    onPreparationClick = { resetTo(AppRoute.Preparation) },
                    onRecordClick = { resetTo(AppRoute.RecordList) },
                    onProfileClick = { resetTo(AppRoute.Profile) },
                )

                is AppRoute.AddCompetition -> AddCompetitionScreen(
                    onBack = { goBack() },
                    // 抽取成功已落库：回到发现页并刷新，让新赛事立刻出现。
                    onExtracted = { resetTo(AppRoute.Discovery) },
                )

                is AppRoute.SchoolCompetition -> SchoolCompetitionScreen(
                    onDiscoverClick = { resetTo(AppRoute.Discovery) },
                    onPreparationClick = { resetTo(AppRoute.Preparation) },
                    onRecordClick = { resetTo(AppRoute.RecordList) },
                    onProfileClick = { resetTo(AppRoute.Profile) },
                    onSearchClick = { navigate(AppRoute.SearchProfile) },
                    onViewDetailClick = { navigate(AppRoute.CompetitionDetail("comp-1")) },
                    onNoticeClick = { navigate(AppRoute.Notice) },
                )

                is AppRoute.Preparation -> PreparationScreen(
                    onDiscoverClick = { resetTo(AppRoute.Discovery) },
                    onSchoolCompetitionClick = { resetTo(AppRoute.SchoolCompetition) },
                    onRecordClick = { resetTo(AppRoute.RecordList) },
                    onProfileClick = { resetTo(AppRoute.Profile) },
                    onViewDetailClick = { navigate(AppRoute.CompetitionDetail("comp-1")) },
                    onContinueLearningClick = { prepId -> navigate(AppRoute.StudyClassroom(prepId)) },
                    onRecordEntryClick = { resetTo(AppRoute.RecordList) },
                    onNoticeClick = { navigate(AppRoute.Notice) },
                )

                is AppRoute.Profile -> ProfileScreen(
                    onDiscoverClick = { resetTo(AppRoute.Discovery) },
                    onSchoolCompetitionClick = { resetTo(AppRoute.SchoolCompetition) },
                    onPreparationClick = { resetTo(AppRoute.Preparation) },
                    onRecordClick = { resetTo(AppRoute.RecordList) },
                    onSettingsClick = { navigate(AppRoute.Settings) },
                )

                is AppRoute.HotProfessional -> HotProfessionalScreen(onBack = { goBack() })

                is AppRoute.SearchProfile -> SearchProfileScreen(
                    onBack = { goBack() },
                    onNext = { navigate(AppRoute.CompetitionDetail("comp-1")) },
                )

                is AppRoute.CompetitionDetail -> CompetitionDetailScreen(
                    competitionId = current.competitionId,
                    onBack = { goBack() },
                    onNext = { navigate(AppRoute.RulesSchedule(current.competitionId)) },
                )

                is AppRoute.RulesSchedule -> RulesScheduleScreen(
                    competitionId = current.competitionId,
                    onBack = { goBack() },
                    onNext = { navigate(AppRoute.ValueAssessment(current.competitionId)) },
                )

                is AppRoute.ValueAssessment -> ValueAssessmentScreen(
                    competitionId = current.competitionId,
                    onBack = { goBack() },
                    onNext = { navigate(AppRoute.RoleSelection(current.competitionId)) },
                )

                is AppRoute.RoleSelection -> RoleSelectionScreen(
                    competitionId = current.competitionId,
                    onBack = { goBack() },
                    onNext = { resetTo(AppRoute.Preparation) },
                )

                is AppRoute.StudyClassroom -> StudyClassroomScreen(
                    preparationId = current.preparationId,
                    onBack = { goBack() },
                    onNext = { navigate(AppRoute.RecordList) },
                )

                is AppRoute.RecordList -> RecordListScreen(
                    onDiscoverClick = { resetTo(AppRoute.Discovery) },
                    onSchoolCompetitionClick = { resetTo(AppRoute.SchoolCompetition) },
                    onRecordDetailClick = { navigate(AppRoute.RecordDetail("rec-1")) },
                    onPreparationClick = { resetTo(AppRoute.Preparation) },
                    onProfileClick = { resetTo(AppRoute.Profile) },
                    onNoticeClick = { navigate(AppRoute.Notice) },
                )

                is AppRoute.Notice -> NoticeScreen(onBack = { goBack() })

                is AppRoute.ProfessionalCategory -> ProfessionalCategoryScreen(
                    onBack = { goBack() },
                    onCategoryClick = { key -> navigate(AppRoute.CategoryCompetitionList(key)) },
                )

                is AppRoute.CategoryCompetitionList -> CategoryCompetitionListScreen(
                    categoryKey = current.categoryKey,
                    onBack = { goBack() },
                )

                is AppRoute.RecentDeadline -> RecentDeadlineScreen(onBack = { goBack() })

                is AppRoute.Settings -> SettingsScreen(
                    onBack = { goBack() },
                    onAccountSecurityClick = { navigate(AppRoute.AccountSecurity) },
                    onPrivacyClick = { navigate(AppRoute.PrivacySettings) },
                    onNotificationClick = { navigate(AppRoute.NotificationSettings) },
                    onFeedbackClick = { navigate(AppRoute.Feedback) },
                    onAboutClick = { navigate(AppRoute.About) },
                )

                is AppRoute.AccountSecurity -> AccountSecurityScreen(onBack = { goBack() })

                is AppRoute.PrivacySettings -> PrivacySettingsScreen(onBack = { goBack() })

                is AppRoute.NotificationSettings -> NotificationSettingsScreen(onBack = { goBack() })

                is AppRoute.Feedback -> FeedbackScreen(onBack = { goBack() })

                is AppRoute.About -> AboutScreen(onBack = { goBack() })

        is AppRoute.RecordDetail -> RecordDetailScreen(
                    recordId = current.recordId,
                    onBack = { goBack() },
                    onNext = { /* 终端页：完成首次用户访谈 */ },
                )
            }
        }

        // 玻璃层：与内容层并列（兄弟），不能放进内容层里。
        // 高亮 Tab 从返回栈里**最后一个 Tab 根路由**推导，而不是当前路由：
        // 否则点击铃铛压入 Notice 等非 Tab 路由后，`toTab()` 会退回 Discovery，
        // 导致底栏 selectedTabIndex 变化，进而触发库里的 onTabSelected 自动跳回发现页（已实测）。
        LiquidGlassBottomBar(
            selectedTab = backStack.selectedTab(),
            onTabSelected = { tab ->
                when (tab) {
                    AppTab.Discovery -> resetTo(AppRoute.Discovery)
                    AppTab.School -> resetTo(AppRoute.SchoolCompetition)
                    AppTab.Preparation -> resetTo(AppRoute.Preparation)
                    AppTab.Record -> resetTo(AppRoute.RecordList)
                    AppTab.Profile -> resetTo(AppRoute.Profile)
                }
            },
            backdrop = backdrop,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * 应用路由。
 *
 * 从无参枚举改为**带参密封类** —— 详情类页面必须知道自己显示哪条数据。
 * 参数名与后端契约的 id 一致（见 `docs/03-架构与接口契约.md`）。
 */
sealed interface AppRoute {
    data object Discovery : AppRoute
    data object AddCompetition : AppRoute
    data object SchoolCompetition : AppRoute
    data object Preparation : AppRoute
    data object Profile : AppRoute
    data object HotProfessional : AppRoute
    data object SearchProfile : AppRoute
    data object ProfessionalCategory : AppRoute
    data class CategoryCompetitionList(val categoryKey: String) : AppRoute
    data object RecentDeadline : AppRoute
    data class CompetitionDetail(val competitionId: String) : AppRoute
    data class RulesSchedule(val competitionId: String) : AppRoute
    data class ValueAssessment(val competitionId: String) : AppRoute
    data class RoleSelection(val competitionId: String) : AppRoute
    data class StudyClassroom(val preparationId: String) : AppRoute
    data object RecordList : AppRoute
    data object Notice : AppRoute
    data class RecordDetail(val recordId: String) : AppRoute
    data object Settings : AppRoute
    data object AccountSecurity : AppRoute
    data object PrivacySettings : AppRoute
    data object NotificationSettings : AppRoute
    data object Feedback : AppRoute
    data object About : AppRoute
}

/** 屏幕到底部 Tab 的映射；向导类屏幕没有对应 Tab。 */
private fun AppRoute.toTab(): AppTab? = when (this) {
    is AppRoute.Discovery -> AppTab.Discovery
    is AppRoute.SchoolCompetition -> AppTab.School
    is AppRoute.Preparation -> AppTab.Preparation
    is AppRoute.RecordList -> AppTab.Record
    is AppRoute.Profile -> AppTab.Profile
    else -> null
}

/**
 * 从返回栈推导底栏应高亮的 Tab：取栈里**最后一个 Tab 根路由**。
 *
 * 为什么不是 `backStack.last().toTab()`：压入 Notice/详情/设置等非 Tab 路由时，
 * 若用栈顶路由映射，会退回 Discovery（或 null），使底栏 selectedTabIndex 抖动，
 * 触发液态玻璃库里的 onTabSelected 自动导航回发现页。
 */
private fun List<AppRoute>.selectedTab(): AppTab =
    asReversed().firstNotNullOfOrNull { it.toTab() } ?: AppTab.Discovery
