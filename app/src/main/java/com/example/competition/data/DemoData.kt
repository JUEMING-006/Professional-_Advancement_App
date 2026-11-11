package com.example.competition.data

import com.example.competition.domain.Award
import com.example.competition.domain.CompetitionDetail
import com.example.competition.domain.CompetitionSummary
import com.example.competition.domain.Deliverable
import com.example.competition.domain.DeliverableItem
import com.example.competition.domain.ExtractDraft
import com.example.competition.domain.ExtractResponse
import com.example.competition.domain.Lesson
import com.example.competition.domain.NextLesson
import com.example.competition.domain.NoticeDto
import com.example.competition.domain.NoticeListResponse
import com.example.competition.domain.Participation
import com.example.competition.domain.Preparation
import com.example.competition.domain.PreparationMember
import com.example.competition.domain.PreparationProgress
import com.example.competition.domain.Profile
import com.example.competition.domain.ProfileOverview
import com.example.competition.domain.ProfileStats
import com.example.competition.domain.Record
import com.example.competition.domain.RecordGroup
import com.example.competition.domain.RecordListResponse
import com.example.competition.domain.RecordStatistics
import com.example.competition.domain.RelatedLesson
import com.example.competition.domain.Role
import com.example.competition.domain.ScheduleNode
import com.example.competition.domain.ScheduleResponse
import com.example.competition.domain.ScoringDimension
import com.example.competition.domain.ScoringRef
import com.example.competition.domain.ScoringResponse
import com.example.competition.domain.SourceRef
import com.example.competition.domain.StudyModule
import com.example.competition.domain.StudyPath
import com.example.competition.domain.ValueAssessmentResponse
import com.example.competition.domain.ValueChecklistItem
import com.example.competition.domain.ValueDimension

/**
 * 演示数据（离线 Mock）。
 *
 * 后端不在本仓库，为了让 App 在没有后端的情况下也能完整跑起来、
 * 展示全部 15 个页面，这里把所有接口的返回数据一次性写死。
 *
 * ⚠️ 这是**演示数据**，不是真实数据：
 * - 赛事名、评分、赛程等均为虚构示例；
 * - 界面多处已有「示例赛事」「以上为产品演示用示例赛事」等免责说明。
 *
 * 内容与 [com.example.competition.AddCompetitionScreen] 里的 SAMPLE_CHARTER
 * 保持一致，保证「添加比赛 → 填入示例 → 识别成功」这条链路自洽。
 */
object DemoData {

    /** 章程原文出处（供评分维度引用）。 */
    private val charterSource = SourceRef(
        label = "《2026 校园数字创新挑战赛章程》",
        url = "https://example.edu.cn/charter/2026-digital-innovation",
        docNo = "团委〔2026〕12 号",
        clauseNo = "四",
        fetchedAt = "2026-11-10T10:00:00+08:00",
    )

    // ===== 赛事列表 =====

    val competitions: List<CompetitionSummary> = listOf(
        CompetitionSummary(
            id = "comp-1",
            name = "2026 校园数字创新挑战赛",
            subtitle = "用数字化思维，解决真实的校园问题",
            trackName = "智慧校园赛道",
            categoryCode = "information",
            typeCode = "campus",
            organizerName = "校团委 · 信息化建设与管理处",
            status = "registering",
            deadline = "2026-11-15T23:59:00+08:00",
            teamSizeText = "3–5 人",
            tags = listOf("智慧校园", "数字化", "小程序"),
            isSample = false,
            isFavorite = true,
            source = charterSource,
        ),
        CompetitionSummary(
            id = "comp-2",
            name = "全国大学生数学建模竞赛",
            subtitle = "用模型与数据，求解现实难题",
            trackName = "本科组",
            categoryCode = "engineering",
            typeCode = "national",
            organizerName = "中国工业与应用数学学会",
            status = "open",
            deadline = "2026-09-10T20:00:00+08:00",
            teamSizeText = "3 人",
            tags = listOf("数学建模", "数据"),
            isSample = true,
            isFavorite = false,
        ),
        CompetitionSummary(
            id = "comp-3",
            name = "大学生创新创业训练计划",
            subtitle = "把想法立项，把项目做扎实",
            trackName = "创业实践类",
            categoryCode = "business",
            typeCode = "national",
            organizerName = "教育部高等教育司",
            status = "running",
            deadline = "2026-06-30T23:59:00+08:00",
            teamSizeText = "3–6 人",
            tags = listOf("创新创业", "项目孵化"),
            isSample = true,
            isFavorite = true,
        ),
        CompetitionSummary(
            id = "comp-4",
            name = "校园文创设计大赛",
            subtitle = "把校园记忆，做成看得见的作品",
            trackName = "文创产品类",
            categoryCode = "art",
            typeCode = "campus",
            organizerName = "校党委宣传部",
            status = "registering",
            deadline = "2026-12-01T23:59:00+08:00",
            teamSizeText = "个人或 2–3 人",
            tags = listOf("文创", "设计"),
            isSample = true,
            isFavorite = false,
        ),
        CompetitionSummary(
            id = "comp-5",
            name = "程序设计天梯赛（校内选拔）",
            subtitle = "组队闯关，代码见真章",
            trackName = "软件工程方向",
            categoryCode = "information",
            typeCode = "college",
            organizerName = "计算机学院",
            status = "upcoming",
            deadline = "2026-12-20T23:59:00+08:00",
            teamSizeText = "3 人",
            tags = listOf("算法", "编程"),
            isSample = true,
            isFavorite = false,
        ),
        CompetitionSummary(
            id = "comp-6",
            name = "市场调研与分析大赛",
            subtitle = "从数据里，读出真实的市场",
            trackName = "调研报告类",
            categoryCode = "business",
            typeCode = "provincial",
            organizerName = "省教育厅",
            status = "closed",
            deadline = "2026-07-20T23:59:00+08:00",
            teamSizeText = "3–5 人",
            tags = listOf("市场调研", "分析"),
            isSample = true,
            isFavorite = false,
        ),
    )

    // ===== 赛事详情（详情/评分/赛程/价值都按 id 取，演示只用 comp-1） =====

    val detail: CompetitionDetail = CompetitionDetail(
        id = "comp-1",
        name = "2026 校园数字创新挑战赛",
        subtitle = "用数字化思维，解决真实的校园问题",
        year = 2026,
        status = "registering",
        categoryName = "信息技术",
        trackName = "智慧校园赛道",
        eligibility = "全日制在校本科生、专科生；允许跨校组队",
        format = "线上初赛 + 线下决赛答辩",
        registrationStart = "2026-10-15T00:00:00+08:00",
        registrationEnd = "2026-11-15T23:59:00+08:00",
        registrationDeadlineText = "",
        minMembers = 3,
        maxMembers = 5,
        allowCrossSchool = true,
        advisorCountMin = 0,
        advisorCountMax = 2,
        fee = 0,
        feeText = "免费",
        organizer = "校团委 · 信息化建设与管理处",
        trackDescription = "围绕智慧校园、智慧学习、智慧生活等方向，用数字化手段解决校园真实问题，产出可演示的作品。",
        trackTags = listOf("智慧校园", "数字化", "小程序", "AI"),
        deliverables = listOf("产品方案文档", "可运行的原型或 Demo", "路演 PPT"),
        awardsDescription = "一等奖 1 队、二等奖 2 队、三等奖 3 队，另设最佳创意奖",
        noticeDocumentUrl = "https://example.edu.cn/notice/2026-digital-innovation",
        isSample = true,
        source = charterSource,
        fetchedAt = "2026-11-10T10:00:00+08:00",
    )

    val details: Map<String, CompetitionDetail> = mapOf("comp-1" to detail)

    val scoring: ScoringResponse = ScoringResponse(
        totalScore = 100,
        dimensions = listOf(
            ScoringDimension(
                id = "dim-1",
                name = "问题洞察",
                score = 30,
                description = "需求真实，有访谈或数据支持，场景清晰。",
                clauseNo = "四.1",
                quote = "问题洞察（30 分）：需求真实，有访谈或数据支持，场景清晰。",
                source = charterSource,
            ),
            ScoringDimension(
                id = "dim-2",
                name = "方案创新",
                score = 25,
                description = "切入点有差异化，不重复既有产品。",
                clauseNo = "四.2",
                quote = "方案创新（25 分）：切入点有差异化，不重复既有产品。",
                source = charterSource,
            ),
            ScoringDimension(
                id = "dim-3",
                name = "完成质量",
                score = 25,
                description = "功能可用、流程完整、细节打磨到位。",
                clauseNo = "四.3",
                quote = "完成质量（25 分）：功能可用、流程完整、细节打磨到位。",
                source = charterSource,
            ),
            ScoringDimension(
                id = "dim-4",
                name = "展示表达",
                score = 20,
                description = "演示清晰、讲述到位、答辩有说服力。",
                clauseNo = "四.4",
                quote = "展示表达（20 分）：演示清晰、讲述到位、答辩有说服力。",
                source = charterSource,
            ),
        ),
    )

    val schedule: ScheduleResponse = ScheduleResponse(
        timezone = "Asia/Shanghai",
        nodes = listOf(
            ScheduleNode(id = "s-1", date = "2026-11-15T23:59:00+08:00", title = "报名截止", description = "网上报名系统关闭", kind = "deadline", isNext = true),
            ScheduleNode(id = "s-2", date = "2026-11-29T23:59:00+08:00", title = "初赛作品提交", description = "提交方案文档与演示视频", kind = "submit"),
            ScheduleNode(id = "s-3", date = "2026-12-05T12:00:00+08:00", title = "入围名单公布", description = "官网与公众号同步发布", kind = "notice"),
            ScheduleNode(id = "s-4", date = "2026-12-12T09:00:00+08:00", title = "决赛答辩", description = "线下路演与评委问答", kind = "final"),
        ),
    )

    val valueAssessment: ValueAssessmentResponse = ValueAssessmentResponse(
        summary = "这场比赛与你的专业方向契合：赛程清晰、评分公开、有明确交付物，适合作为学期内的备赛项目。",
        dimensions = listOf(
            ValueDimension(
                name = "组织认可",
                status = "verified",
                description = "校团委与信息化处联合主办，可计入第二课堂学分。",
                source = SourceRef(label = "校团委通知", url = "https://example.edu.cn/notice/org"),
            ),
            ValueDimension(
                name = "评审规则",
                status = "verified",
                description = "评分细则公开透明，四个维度各有明确权重。",
                source = SourceRef(label = "《章程》四、初赛评分"),
            ),
            ValueDimension(
                name = "作品能力",
                status = "partial",
                description = "能沉淀一份可演示的作品，直接放进作品集。",
                source = SourceRef(label = "往届获奖作品展示页"),
            ),
            ValueDimension(
                name = "时间投入",
                status = "verified",
                description = "约 4 周、每周 4 小时，与课业可兼容。",
                source = SourceRef(label = "赛程安排"),
            ),
        ),
        checklist = listOf(
            ValueChecklistItem(id = "c-1", text = "向学院确认，是否计入竞赛或实践学分"),
            ValueChecklistItem(id = "c-2", text = "与队友确认，每周能投入的时间"),
        ),
    )

    // ===== 岗位 =====

    val roles: List<Role> = listOf(
        Role(
            id = "role-1",
            name = "产品策划",
            description = "负责需求调研、方案设计与项目统筹，是团队与用户的桥梁。",
            learningFocus = "需求调研与产品方案",
            iconKey = "lightbulb",
            alignedScoringDimensionIds = listOf("dim-1", "dim-2"),
        ),
        Role(
            id = "role-2",
            name = "视觉设计",
            description = "负责界面与视觉表达，让作品既好用又好看。",
            learningFocus = "界面与视觉表达",
            iconKey = "pen",
            alignedScoringDimensionIds = listOf("dim-4"),
        ),
        Role(
            id = "role-3",
            name = "前端开发",
            description = "负责小程序与交互实现，把方案变成可运行的产品。",
            learningFocus = "小程序与交互实现",
            iconKey = "code",
            alignedScoringDimensionIds = listOf("dim-3"),
        ),
        Role(
            id = "role-4",
            name = "数据分析",
            description = "负责数据洞察与论证，用数据支撑需求与效果。",
            learningFocus = "数据洞察与论证",
            iconKey = "chart",
            alignedScoringDimensionIds = listOf("dim-1", "dim-3"),
        ),
    )

    // ===== 学习路径与课时 =====

    val studyPath: StudyPath = StudyPath(
        id = "path-1",
        pathName = "产品策划专属学习路径",
        tagline = "从需求到方案，一步步做出可答辩的作品",
        completedLessons = 2,
        totalLessons = 6,
        weeklyHours = 4,
        targetDate = "2026-11-29",
        modules = listOf(
            StudyModule(
                id = "m-1",
                name = "需求与洞察",
                description = "理解用户与场景",
                completedLessons = 2,
                totalLessons = 2,
                status = "done",
                lessons = listOf(
                    Lesson(
                        id = "lesson-1",
                        label = "第 1 节",
                        title = "认识用户访谈",
                        durationMinutes = 30,
                        type = "video",
                        alignedScoringDimensionId = "dim-1",
                        deliverableText = "访谈提纲",
                    ),
                    Lesson(
                        id = "lesson-2",
                        label = "第 2 节",
                        title = "整理需求与洞察",
                        durationMinutes = 30,
                        type = "practice",
                        alignedScoringDimensionId = "dim-1",
                        deliverableText = "用户访谈记录",
                    ),
                ),
            ),
            StudyModule(
                id = "m-2",
                name = "方案与原型",
                description = "把洞察变成方案",
                completedLessons = 0,
                totalLessons = 2,
                status = "in_progress",
                lessons = listOf(
                    Lesson(
                        id = "lesson-3",
                        label = "第 1 节",
                        title = "画出你的产品原型",
                        durationMinutes = 45,
                        type = "practice",
                        alignedScoringDimensionId = "dim-2",
                        deliverableText = "低保真原型图",
                    ),
                    Lesson(
                        id = "lesson-4",
                        label = "第 2 节",
                        title = "撰写产品方案文档",
                        durationMinutes = 40,
                        type = "doc",
                        alignedScoringDimensionId = "dim-2",
                        deliverableText = "产品方案文档",
                    ),
                ),
            ),
            StudyModule(
                id = "m-3",
                name = "展示与答辩",
                description = "把作品讲清楚",
                completedLessons = 0,
                totalLessons = 2,
                status = "not_started",
                lessons = listOf(
                    Lesson(
                        id = "lesson-5",
                        label = "第 1 节",
                        title = "路演 PPT 结构",
                        durationMinutes = 35,
                        type = "video",
                        alignedScoringDimensionId = "dim-4",
                        deliverableText = "路演 PPT",
                    ),
                    Lesson(
                        id = "lesson-6",
                        label = "第 2 节",
                        title = "答辩常见问题",
                        durationMinutes = 30,
                        type = "video",
                        alignedScoringDimensionId = "dim-4",
                        deliverableText = "答辩要点",
                    ),
                ),
            ),
        ),
    )

    val nextLesson: NextLesson = NextLesson(
        lessonId = "lesson-3",
        pathLabel = "产品策划专属学习路径",
        moduleName = "方案与原型",
        lessonIndex = 1,
        title = "画出你的产品原型",
        durationText = "约 45 分钟",
        alignedScoring = ScoringRef(dimensionId = "dim-2", name = "方案创新", score = 25),
        deliverableText = "低保真原型图 + 交互说明",
        topicNote = "把需求洞察转成可演示的原型，重点是流程闭环。",
    )

    // ===== 备赛项目 =====

    val preparations: List<Preparation> = listOf(
        Preparation(
            id = "prep-1",
            competitionId = "comp-1",
            competitionName = "2026 校园数字创新挑战赛",
            trackName = "智慧校园赛道",
            teamSizeText = "3–5 人",
            status = "learning",
            keyDates = listOf("报名截止 11.15", "初赛提交 11.29"),
            roleId = "role-1",
            roleName = "产品策划",
            teamName = "拾光小队",
            projectIdea = "校园闲置物品循环平台",
            members = listOf(
                PreparationMember(id = "u-1", displayName = "林小满", roleId = "role-1", joinedAt = "2026-10-16"),
                PreparationMember(id = "u-2", displayName = "陈晨", roleId = "role-3", joinedAt = "2026-10-16"),
                PreparationMember(id = "u-3", displayName = "苏苏", roleId = "role-2", joinedAt = "2026-10-17"),
            ),
            progress = PreparationProgress(completed = 2, total = 6, percent = 33),
            pathName = "产品策划专属学习路径",
        ),
    )

    // ===== 记录 =====

    val records: RecordListResponse = RecordListResponse(
        statistics = RecordStatistics(totalRecords = 5, totalMinutes = 180, deliverableCount = 2),
        groups = listOf(
            RecordGroup(
                date = "11月9日",
                count = 2,
                items = listOf(
                    Record(
                        id = "rec-1",
                        type = "practice",
                        title = "完成 5 位同学的访谈",
                        summary = "围绕校园闲置物品流转的痛点，访谈了 5 位同学，整理出高频需求：定价、信任、配送。",
                        occurredAt = "2026-11-09 20:30",
                        durationMinutes = 60,
                        deliverable = Deliverable(
                            id = "d-1",
                            name = "用户访谈记录",
                            version = "v1",
                            items = listOf(
                                DeliverableItem(id = "di-1", text = "访谈提纲（10 问）"),
                                DeliverableItem(id = "di-2", text = "5 份访谈原始记录"),
                            ),
                        ),
                        relatedLesson = RelatedLesson(moduleName = "需求与洞察", lessonIndex = 2),
                        projectId = "prep-1",
                    ),
                    Record(
                        id = "rec-2",
                        type = "lesson",
                        title = "学习：如何做好用户访谈",
                        summary = "完成课程「用户访谈方法」，掌握了开放式提问与追问技巧。",
                        occurredAt = "2026-11-09 19:00",
                        durationMinutes = 30,
                        relatedLesson = RelatedLesson(moduleName = "需求与洞察", lessonIndex = 1),
                        projectId = "prep-1",
                    ),
                ),
            ),
            RecordGroup(
                date = "11月6日",
                count = 1,
                items = listOf(
                    Record(
                        id = "rec-3",
                        type = "deliverable",
                        title = "产出：需求调研报告",
                        summary = "整理访谈与二手数据，输出一份需求调研报告。",
                        occurredAt = "2026-11-06 21:00",
                        durationMinutes = 45,
                        deliverable = Deliverable(
                            id = "d-2",
                            name = "需求调研报告",
                            version = "v1",
                            items = listOf(
                                DeliverableItem(id = "di-3", text = "用户画像 3 类"),
                                DeliverableItem(id = "di-4", text = "核心痛点清单"),
                            ),
                        ),
                        relatedLesson = RelatedLesson(moduleName = "需求与洞察", lessonIndex = 2),
                        projectId = "prep-1",
                    ),
                ),
            ),
            RecordGroup(
                date = "11月2日",
                count = 2,
                items = listOf(
                    Record(
                        id = "rec-4",
                        type = "lesson",
                        title = "学习：产品定位与价值主张",
                        summary = "理解「为谁解决什么问题」，初步确定项目切入点。",
                        occurredAt = "2026-11-02 20:00",
                        durationMinutes = 25,
                        relatedLesson = RelatedLesson(moduleName = "方案与原型", lessonIndex = 1),
                        projectId = "prep-1",
                    ),
                    Record(
                        id = "rec-5",
                        type = "practice",
                        title = "组建团队并分工",
                        summary = "确定拾光小队 3 人分工：产品、开发、设计。",
                        occurredAt = "2026-11-02 18:30",
                        durationMinutes = 20,
                        projectId = "prep-1",
                    ),
                ),
            ),
        ),
    )

    // ===== 通知 =====

    val notices: NoticeListResponse = NoticeListResponse(
        items = listOf(
            NoticeDto(
                id = "n-1",
                competitionKey = "comp-1",
                competitionName = "2026 校园数字创新挑战赛",
                title = "关于延长报名截止时间的通知",
                url = "https://example.edu.cn/notice/1",
                publishedDate = "2026-11-08",
                sourceHost = "校团委官网",
                isRead = false,
            ),
            NoticeDto(
                id = "n-2",
                competitionKey = "comp-2",
                competitionName = "全国大学生数学建模竞赛",
                title = "2026 年竞赛报名启动",
                url = "https://example.edu.cn/notice/2",
                publishedDate = "2026-11-05",
                sourceHost = "数模竞赛官网",
                isRead = false,
            ),
            NoticeDto(
                id = "n-3",
                competitionKey = "comp-4",
                competitionName = "校园文创设计大赛",
                title = "作品提交方式说明",
                url = "https://example.edu.cn/notice/3",
                publishedDate = "2026-10-28",
                sourceHost = "校党委宣传部",
                isRead = true,
            ),
        ),
        total = 3,
        unreadCount = 2,
    )

    // ===== 我的 =====

    val profile: Profile = Profile(
        displayName = "林小满",
        avatarInitial = "林",
        school = "示例大学",
        major = "计算机科学与技术",
        grade = "2024 级",
        interestDirections = listOf("智慧校园", "小程序开发", "产品策划"),
    )

    val profileOverview: ProfileOverview = ProfileOverview(
        stats = ProfileStats(competitions = 3, awards = 2, records = 5),
        awards = listOf(
            Award(
                id = "a-1",
                level = "国家级",
                name = "中国大学生计算机设计大赛 二等奖",
                time = "2025.08",
                meta = "项目：校园失物招领小程序",
            ),
            Award(
                id = "a-2",
                level = "校级",
                name = "校园创新创业大赛 一等奖",
                time = "2025.05",
                meta = "项目：智慧课堂签到系统",
            ),
        ),
        participations = listOf(
            Participation(
                id = "p-1",
                name = "中国大学生计算机设计大赛",
                session = "2025",
                period = "2025.03–2025.08",
                role = "产品策划",
                result = "二等奖",
                awarded = true,
                status = "ended",
            ),
            Participation(
                id = "p-2",
                name = "校园创新创业大赛",
                session = "2025",
                period = "2025.02–2025.05",
                role = "前端开发",
                result = "一等奖",
                awarded = true,
                status = "ended",
            ),
            Participation(
                id = "p-3",
                name = "程序设计天梯赛校内选拔",
                session = "2024",
                period = "2024.10–2024.11",
                role = "前端开发",
                result = "完赛",
                awarded = false,
                status = "ended",
            ),
        ),
    )

    // ===== 智能体抽取 =====

    /**
     * 「添加比赛」的抽取结果。
     *
     * 演示版直接返回一个 `valid=true` 的结果，draft 内容与
     * SAMPLE_CHARTER 一致，让「填入示例 → 识别成功」这条链路能走通。
     */
    val extractResult: ExtractResponse = ExtractResponse(
        valid = true,
        errors = emptyList(),
        competition = null,
        draft = ExtractDraft(
            name = "2026 校园数字创新挑战赛",
            organizer = "校团委 · 信息化建设与管理处",
            totalScore = 100,
            scoring = scoring.dimensions,
            schedule = schedule.nodes,
        ),
        notes = "演示数据：本结果由离线 Mock 生成，非真实模型输出。",
    )
}
