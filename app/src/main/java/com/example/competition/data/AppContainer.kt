package com.example.competition.data

import android.app.Application

/**
 * 简单的应用级依赖容器。
 *
 * 演示版：只提供一个离线的 [CompetitionRepository]，不接网络、不读 BuildConfig。
 * 界面与 ViewModel 仍通过 `app.container.repository` 拿数据，写法不变。
 */
class AppContainer(application: Application) {
    val repository: CompetitionRepository by lazy {
        CompetitionRepository()
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
