package com.example.competition.data

import android.app.Application
import com.example.competition.BuildConfig

/**
 * 简单的应用级依赖容器。
 *
 * 现阶段不引入 Hilt —— 依赖只有 ApiClient 与 Repository 两层，
 * 手写容器更直白，也少一个第三方依赖（AGENTS.md 铁律：新增依赖需先问）。
 */
class AppContainer(application: Application) {
    val apiClient: ApiClient by lazy {
        ApiClient(BuildConfig.API_BASE_URL)
    }

    val repository: CompetitionRepository by lazy {
        CompetitionRepository(apiClient)
    }
}

class CompetitionApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
