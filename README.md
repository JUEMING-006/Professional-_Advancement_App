# 竞学 App — Android 客户端

> 校园赛事发现与备赛 App。Kotlin + Jetpack Compose + 液态玻璃（Liquid Glass）UI。
> 后端是独立的 Kotlin/Spring Boot 服务，**不在本仓库**。

---

## 一、快速开始（3 步）

### 1. 确认环境

| 项 | 要求 |
|---|---|
| JDK | **21**（项目用 toolchain 钉死，不要用其它版本） |
| Android SDK | compileSdk **37**、build-tools **37.0.0** |
| Gradle | **不用装**，用项目自带的 wrapper（9.6.0） |

> ⚠️ 如果你的系统 `JAVA_HOME` 指向别的 JDK，Gradle 会自动下载 JDK 21（foojay resolver）。
> 想手动指定：`set JAVA_HOME=<你的JDK21路径>`

### 2. 配置 `local.properties`

```bash
cp local.properties.example local.properties
```

然后编辑 `local.properties`，**只改这两行**：

```properties
# 改成你机器上的 Android SDK 路径
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk

# 后端地址（模拟器保持默认 10.0.2.2 即可）
api.baseUrl=http://10.0.2.2:8080
```

> **为什么不能直接用别人的 `local.properties`**：里面是绝对路径，每人机器不同。

### 3. 构建

```bash
# Windows
.\gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

---

## 二、跑起来需要后端

App 的数据**全部来自后端**，后端不开会显示「加载失败」。

后端地址由 `local.properties` 的 `api.baseUrl` 注入到 `BuildConfig.API_BASE_URL`。

| 场景 | `api.baseUrl` 该填什么 |
|---|---|
| **模拟器** | `http://10.0.2.2:8080`（10.0.2.2 是模拟器访问宿主机的固定地址） |
| **真机** | `http://<电脑局域网IP>:8080`，且手机与电脑**连同一个 WiFi** |

查看电脑局域网 IP：
```powershell
# Windows
ipconfig
# macOS / Linux
ifconfig | grep "inet "
```

改完 `local.properties` 后**必须重新构建**（地址是编译期注入的，不是运行期读取）。

---

## 三、项目结构

```
Competition/
├── app/                          主 App 模块
│   └── src/main/java/com/example/competition/
│       ├── MainActivity.kt       单 Activity + 手写路由（AppRoute 密封接口）
│       ├── *Screen.kt            15 个页面
│       ├── domain/Contracts.kt   ★ 与后端契约逐字对应的数据模型
│       ├── data/                 ← 网络层
│       │   ├── ApiClient.kt      OkHttp + kotlinx.serialization
│       │   ├── CompetitionRepository.kt
│       │   └── AppContainer.kt   手写依赖容器（无 Hilt）
│       └── ui/
│           ├── common/           ★ 共享基础设施（见下）
│           ├── components/       液态玻璃组件
│           ├── theme/            颜色/字号/尺寸常量
│           └── <feature>/        各页 ViewModel
└── liquidglass/                  ★ 液态玻璃源码模块（**不要删**）
```

### 关键设计（改代码前请先读）

#### 1. `ui/common/LoadState.kt` —— 所有数据页共用

```kotlin
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Failure(val message: String, val retryable: Boolean = true) : LoadState<Nothing>
}

abstract class LoadableViewModel<T> : ViewModel() {
    protected abstract suspend fun fetch(): T?   // 只需实现这一个方法
    // ensureLoaded() / reload() / state 都是现成的
}
```

**新增一个数据页时，ViewModel 只需要写 20 行左右**，不要重新写 `loading/error/retry` 三件套。

#### 2. `ui/common/StateViews.kt` —— 三种状态视图

`LoadingView` / `ErrorView` / `EmptyView`，直接复用，不要每页各写一份。

#### 3. 契约红线（**改接口时绝对不能破坏**）

`docs/03-架构与接口契约.md` 定义了 12 条红线，其中对客户端影响最大的：

| 红线 | 含义 |
|---|---|
| 不得出现 `fraction` / `highlighted` | 后端不下发"进度比例""是否高亮"，这类信息由前端自己算 |
| 不得出现分数/概率/排名 | 除了章程原文里的分值，不得有任何"综合评分""获奖概率""含金量排名" |
| 每条评分必须带 `quote` | 评分维度的 `quote` 是章程原文引用，**界面必须展示**，这是防编造的证据 |
| 数据必须带 `source` | 没有来源的数据不允许显示 |

**代码里已经落实**：`RulesScheduleScreen` 曾经本地定义过含 `fraction` 的 `ScoringDimension`，已删除，统一用契约类型。**不要加回去。**

#### 4. 液态玻璃的硬约束

`liquidglass/` 是**源码模块**（不是 Maven 依赖），必须一起编译。

玻璃组件的结构铁律：
```kotlin
Box {
    // 内容层：先记录，供玻璃采样
    Box(Modifier.layerBackdrop(backdrop)) { /* 页面内容 */ }
    // 玻璃层：必须是内容层的**兄弟节点**
    GlassBottomBar(backdrop = backdrop, Modifier.align(BottomCenter))
}
```

⚠️ **不要把玻璃组件放进内容层内部** —— 会导致递归 500+ 层栈溢出（已踩过，native 崩溃无堆栈）。
⚠️ **不要给可滚动容器加 `layerBackdrop`** —— 同样栈溢出。

依赖版本**锁定**：
- `io.github.kyant0:backdrop:1.0.6`
- `io.github.kyant0:shapes:1.2.0`

**不要升到 2.x** —— 需要 Compose 1.12 + AGP 9.1+，当前工具链不兼容。

#### 5. 底部内容边距

`MainActivity` 给内容层留了 `padding(bottom = 88.dp)`，因为玻璃底栏是**浮在上面的兄弟节点**。
去掉这个 padding 会导致页面自己的底部按钮被底栏盖住（已实测）。

---

## 四、当前完成度

| 页面 | 数据接入 | 懒加载 |
|---|---|---|
| 发现页 | ✅ | — |
| 校赛页 | ✅ | ✅ |
| 备赛页 | ✅ | ✅ |
| 记录页 | ✅ | ✅ |
| 我的页 | ✅ | ✅ |
| 赛事详情 | ✅ | ✅ |
| 评分细则 | ✅ | ✅ |
| 价值评估 | ✅ | ✅ |
| 岗位选择 | ✅ | ✅ |
| 学习课堂 | ✅ | ✅ |
| 记录详情 | ✅ | ✅ |
| 热门专业 | ✅ | ✅ |
| 搜索画像 | ✅ | ✅ |
| 赛事通知 | ✅ | ✅ |
| 添加比赛 | ✅ | — |

**13/15 屏**已接后端数据 + 使用 `LazyColumn`。

---

## 五、已知待优化点（适合接手）

按优先级：

### P1 — 架构
1. **手写路由没有返回栈**
   `MainActivity` 用 `mutableStateOf<AppRoute>`，`goBack()` 是空实现。
   从详情页返回时列表页会重新加载、状态丢失。
   建议换 `Navigation Compose`（需新增依赖）。

2. **无统一错误提示**
   网络失败只显示「加载失败」，没有区分「无网络」「超时」「服务端错误」。

### P2 — 体验
3. **搜索框是假的**
   发现页和校赛页的搜索框**不能输入**，只是样式。
   后端已支持 `?keyword=`，接上即可。

4. **首页推荐卡片写死**
   `CompetitionDiscoveryScreen` 的 `RecommendCard` 是硬编码文案。

5. **筛选器没有实际过滤**
   校赛页的「比赛类型/主办学院」筛选 chip 点击无效果。

### P3 — 工程
6. **无单元测试**
   契约模型（`Contract`）应补 JSON 编解码测试，防止后端改字段前端静默失败。

7. **无 CI**
   建议加一个 GitHub Actions：`gradlew :app:assembleDebug` + `lint`。

8. **`applicationId` 是 `com.example.competition`**
   上架前必须改（`com.example` 不允许发布）。

---

## 六、常见问题

### 构建报 `SDK location not found`
`local.properties` 没建或 `sdk.dir` 路径不对。

### 构建报 `Unsupported class file major version`
`JAVA_HOME` 指向了不兼容的 JDK。要么改成 JDK 21，要么让 foojay 自动下载（删掉 `JAVA_HOME` 环境变量）。

### 构建很慢 / 卡住
项目在 Windows 上构建+模拟器同时跑会内存不足（实测卡死 14 分钟）。**构建时关掉模拟器**。

### App 显示「加载失败」
1. 后端没启动
2. `api.baseUrl` 填错（模拟器要 `10.0.2.2`，不是 `127.0.0.1`）
3. 真机时手机与电脑不在同一 WiFi
4. 改了 `local.properties` 但没有重新构建

### 玻璃效果卡顿
液态玻璃是实时折射计算，低端机或**软件渲染的模拟器**上必然掉帧。
模拟器如果用的是 SwiftShader（纯软件渲染），单帧可能超过 15 秒，表现为 ANR。
→ 换真机测试，或给模拟器开 GPU 加速。

### 想看后端接口文档
在交付包的 `docs/03-架构与接口契约.md`（如果提供了文档目录）。

---

## 七、给接手人的话

这个工程的核心价值在两点：

1. **智能体抽取链路**：用户粘贴赛事章程 → 后端用大模型抽出评分维度 → **每个维度都带原文引用** → 校验不通过不入库。
   前端负责展示 `quote`，这是产品可信度的来源，**不要为了好看把引用块删掉**。

2. **液态玻璃 UI**：这是产品的视觉差异点，但也是性能成本最高的部分。
   如果要做性能优化，先从这里下手（比如只在关键页面启用折射）。

代码里凡是标了 ⚠️ 的注释，都是踩过坑之后写的，**动手前请先读**。
