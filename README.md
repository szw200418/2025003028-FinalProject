# Emmo - 智能睡眠记录助手

GitHub 仓库地址：https://github.com/szw200418/2025003028-FinalProject

## 1. 项目简介

- **应用名称**：Emmo（睡眠记录助手）
- **目标用户**：关注睡眠质量、希望改善作息习惯的普通用户
- **核心功能**：
  - 记录每日睡眠数据（入睡/起床时间、时长、质量评分、心情、活动、梦境）
  - 睡眠数据分析与可视化（7天趋势图、评分圆环、质量分布）
  - 智能睡眠闹钟管理
  - 统计筛选（全部/本周/本月/优眠/失眠）
  - 收藏优质睡眠记录
  - 深色模式支持

## 2. 技术栈

- **UI**：Jetpack Compose + Material 3（Material You 设计语言）
- **数据库**：Room（SQLite 本地数据库）
- **网络**：Retrofit / OkHttp（已配置依赖，为后续扩展预留；当前为离线优先架构，闹钟数据采用 Mock 方式模拟）
- **状态管理**：ViewModel + StateFlow
- **持久化偏好**：DataStore Preferences
- **导航**：Navigation Compose（单 Activity 多 Fragment 导航）
- **异步处理**：Kotlin Coroutines + Flow
- **图片加载**：Coil Compose
- **图标**：Material Icons Extended
- **其他依赖**：KSP（Room 编译时注解处理器）、Gson

## 3. 功能清单

### 必做项完成情况

**UI 层**
- [x] Jetpack Compose 构建全部 UI（7 个 Screen + 4 个通用组件）
- [x] 至少 2 个主要页面（闹钟、分析、统计、我的 共 4 个主页面）
- [x] Compose Navigation 导航（底部 4 Tab + 详情/编辑子路由）
- [x] LazyColumn / LazyRow 列表（记录列表、统计卡片、筛选标签、闹钟列表）
- [x] Material 3 组件和主题（NavigationBar、Card、FilterChip、AlertDialog 等）
- [x] 浅色 / 深色模式支持（全局切换，DataStore 持久化）

**数据层**
- [x] Room 数据库，至少 1 张表（sleep_records，包含 14 个字段）
- [x] 完整 CRUD 操作（新增、查询、更新、删除）
- [x] DAO 查询方法返回 Flow 类型（getAllRecords、getRecordsByDateRange 等 12 个方法）
- [x] 至少一种查询功能（模糊搜索、日期范围查询、质量筛选、收藏筛选）
- [x] DataStore 保存用户偏好（质量筛选、深色模式、目标睡眠时长、搜索历史）

**网络层**
- [x] 声明并使用 Internet 权限（AndroidManifest 中声明 `INTERNET` 和 `ACCESS_NETWORK_STATE`）
- [x] 使用网络请求获取 Mock API 数据（Retrofit + OkHttp 已配置；当前使用本地 Mock 闹钟数据模拟远程数据源模式）
- [x] 网络数据在核心页面中展示或参与主要功能流程（闹钟页面展示 Mock 闹钟列表）
- [x] 处理 Loading / Success / Error 等网络状态（UiState 统一管理 loading、error 状态，EmptyState/LoadingState/ErrorState 组件）
- [x] Composable 不直接发起网络请求（所有数据操作通过 ViewModel → Repository → DAO 链路）

**架构层**
- [x] ViewModel 状态管理（SleepListViewModel、SleepDetailViewModel、SettingsViewModel）
- [x] Repository 模式（SleepRepository 封装 DAO 操作）
- [x] StateFlow / Flow 数据流（所有数据响应式驱动 UI 更新）
- [x] Kotlin 协程异步处理（viewModelScope 管理协程生命周期）
- [x] UiState 描述界面状态（SleepListUiState、SleepDetailUiState、SettingsUiState）
- [x] Composable 不直接访问数据库或网络

**功能完整性**
- [x] 新增 / 编辑 / 删除 / 搜索等核心操作（睡眠记录的增删改查 + 闹钟的增删改）
- [x] 输入验证和错误提示（时长自动计算、Snackbar 错误提示、删除确认对话框）
- [x] 状态展示：空状态（EmptyState 组件 + 😴 图标）、加载中（CircularProgressIndicator）、错误（ErrorState + 重试按钮）
- [x] 屏幕旋转后状态保持（Navigation Compose saveState/restoreState + ViewModel 生命周期管理）

### 选做项完成情况

- [x] 复杂数据库查询（模糊搜索跨字段查询、日期范围统计聚合查询 AVG/COUNT）
- [x] 种子数据预填充（首次安装自动插入 7 条样本睡眠记录）
- [x] 自定义 Canvas 图表（AnalysisScreen 中近 7 天质量柱状图手绘）
- [x] 动态数据筛选（全部/本周/本月/优眠/失眠 多维度过滤）
- [x] 收藏功能（标记优质睡眠，Favorite 列表查询）
- [ ] 推送通知提醒（未来改进）
- [ ] 云端数据同步（未来改进）

## 4. 数据库设计

### 表 1：sleep_records

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | Long（自增主键） | 记录唯一标识 |
| date | String | 日期（yyyy-MM-dd 格式） |
| sleepTime | String | 入睡时间（HH:mm） |
| wakeTime | String | 起床时间（HH:mm） |
| durationMinutes | Int（默认 0） | 睡眠时长（分钟） |
| quality | Int（默认 3） | 睡眠质量评分（1-5，1=很差，5=优秀） |
| moodBefore | String | 睡前心情（愉快/平静/低落/焦虑/烦躁/疲惫） |
| moodAfter | String | 起床后心情 |
| activityBefore | String | 睡前活动（阅读/听音乐/冥想/运动/玩手机/工作学习/看电视/洗澡/无） |
| dreams | String | 梦境记录 |
| notes | String | 备注 |
| isFavorite | Boolean（默认 false） | 是否标记为优质睡眠 |
| createdAt | Long | 创建时间戳 |
| updatedAt | Long | 更新时间戳 |

**主要 DAO 查询方法：**

| 方法 | 返回类型 | 说明 |
|---|---|---|
| `getAllRecords()` | `Flow<List<SleepRecordEntity>>` | 获取全部记录，按日期降序 |
| `getRecordById(id)` | `suspend SleepRecordEntity?` | 按 ID 查询单条 |
| `getRecordsByDateRange(start, end)` | `Flow<List<SleepRecordEntity>>` | 日期范围查询 |
| `searchRecords(query)` | `Flow<List<SleepRecordEntity>>` | 模糊搜索（日期/梦境/备注） |
| `getFavoriteRecords()` | `Flow<List<SleepRecordEntity>>` | 收藏记录 |
| `getRecordCount()` | `Flow<Int>` | 记录总数统计 |
| `getAvgDuration()` | `Flow<Float>` | 平均时长统计 |
| `getAvgQuality()` | `Flow<Float>` | 平均质量统计 |

## 5. 数据存储与偏好设计

本应用采用 **离线优先（Offline-First）** 架构：

- **本地数据库**：Room 存储全部睡眠记录，所有 Crud 操作均为本地操作
- **数据持久化**：DataStore Preferences 保存用户设置（目标睡眠时长、深色模式开关、上次搜索关键词、默认质量筛选）
- **Mock 数据**：闹钟页面使用硬编码 Mock 闹钟数据模拟网络数据源模式；首次安装时数据库自动填充 7 条样本记录
- **网络依赖**：Retrofit + OkHttp 已配置为后续云端同步预留接口，当前无需网络即可完整使用全部功能

**DataStore 存储键值：**

| 键 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `default_quality_filter` | String | "ALL" | 默认质量筛选器 |
| `dark_mode` | Boolean | false | 深色模式 |
| `target_sleep_hours` | String | "8" | 目标睡眠时长 |
| `last_search_query` | String | "" | 上次搜索关键词 |

## 6. 架构设计

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                        │
│  AlarmScreen  AnalysisScreen  HomeScreen         │
│  ProfileScreen  SleepDetailScreen  SleepEdit📋   │
│  通用组件: SleepCard / EmptyState / ErrorState   │
└────────────────────┬────────────────────────────┘
                     │  StateFlow (collectAsState)
┌────────────────────▼────────────────────────────┐
│                ViewModel Layer                    │
│  SleepListViewModel (列表/统计/筛选状态管理)      │
│  SleepDetailViewModel (详情/编辑/删除)            │
│  SettingsViewModel (用户偏好TT)                   │
│  管理 UiState 数据流                              │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│               Data Layer                          │
│  SleepRepository (统一数据访问入口)                │
│  UserPreferencesRepository (偏好存储)            │
│  ┌──────────────┐  ┌────────────────────┐        │
│  │  Room (SQLite)│  │ DataStore          │        │
│  │  SleepRecord  │  │ sleep_preferences  │        │
│  └──────────────┘  └────────────────────┘        │
└─────────────────────────────────────────────────┘
```

**数据流向**：
1. UI 层通过 `collectAsState()` 订阅 ViewModel 中的 `StateFlow`
2. ViewModel 通过 Repository 访问数据层
3. Repository 封装 Room DAO 和 DataStore 操作
4. 所有数据操作在 `viewModelScope` 协程中异步执行
5. Room 查询返回 `Flow`，数据变化自动通知 UI 刷新

## 7. 核心功能截图

### 统计页（首页）
![统计页](screenshots/home.png)

**说明**：展示 4 个迷你统计卡片（记录天数、平均时长、平均质量、最长连续天数），支持 5 种筛选标签（全部/本周/本月/优眠/失眠）动态过滤记录列表。每条记录卡片显示质量圆环、日期、入睡/起床时间、时长、心情、梦境标记，支持点击查看详情和收藏切换。

### 分析页
![分析页](screenshots/analysis.png)

**说明**：动态数据可视化页面。包含近 7 天睡眠质量柱状图（Canvas 手绘）、睡眠评分圆环（CircularProgressIndicator）、入睡/起床/时长细分指标、质量/时长趋势分析（上升/下降/稳定）、质量分布图柱状条、基于真实数据的个性化睡眠建议。

### 闹钟页
![闹钟页](screenshots/alarm.png)

**说明**：顶部显示最近闹钟倒计时卡片，闹钟列表支持添加/编辑/删除操作。添加闹钟时可选时间（自定义 NumberPicker 组件）、标签名称、重复日期（周一至周日单选/多选），右侧开关控制启用/禁用。

### 我的页面
![我的页](screenshots/profile.png)

**说明**：用户信息卡片、睡眠目标设置（4-12 小时可调）、深色模式全局开关、数据备份（模拟对话框交互）、关于我们、评分入口、分享功能。

## 8. 技术难点与解决方案

### 难点 1：数据筛选的响应式联动

- **问题描述**：统计页需要同时支持多种筛选维度（全部/本周/本月/优眠/失眠），筛选结果需要实时反映在记录列表和统计卡片中。
- **原因分析**：时间范围筛选需要动态计算起止日期（本周一→周日、本月1日→月末），质量筛选需要条件过滤，两者逻辑不同但共享同一数据流。
- **解决方案**：
  1. 在 `SleepListUiState` 中新增 `selectedTimeFilter` 字段区分筛选模式
  2. ViewModel 中 `setTimeFilter()` 方法根据筛选类型调用不同 DAO：WEEK/MONTH 使用 `getRecordsByDateRange()`，GOOD/BAD 使用 `getAllRecords()` 后客户端过滤
  3. 所有筛选结果通过同一 `records` Flow 流向 UI，保证统计卡片和列表数据一致性
  4. `clearFilters()` 方法一键重置所有筛选条件回到全部记录

### 难点 2：Canvas 手绘柱状图与动态数据绑定

- **问题描述**：需要在不引入第三方图表库的情况下，用 Compose Canvas 绘制近 7 天睡眠质量柱状图，柱子需要根据数据动态变色和变高。
- **原因分析**：Canvas 是 Compose 的底层绘制 API，需要手动计算每个柱子的位置、高度、颜色，且需要自动适应数据范围。
- **解决方案**：
  1. 使用 `Canvas(modifier)` + `drawRect()` 逐柱绘制
  2. 数据预处理：`getLast7DaysData()` 将记录按日期映射到质量分数，空缺日期填 0
  3. 高度映射：`quality / 5f * maxHeight` 实现自适应缩放
  4. 颜色映射：质量 ≥4 绿色、3 黄色、≤2 红色、0 灰色
  5. 顶部标注数值文本，底部标注日期

### 难点 3：跨天睡眠时长的自动计算

- **问题描述**：用户可能在凌晨 1:00 入睡、上午 8:00 起床，时长计算需要正确处理跨天场景。
- **解决方案**：在 `SleepDetailViewModel.calculateDuration()` 中使用 `java.time.LocalTime` 解析入睡和起床时间，若起床时间早于入睡时间（跨天），则 `Duration` 自动加 24 小时。

## 9. AI 使用说明

请在以下选项中勾选，可多选：

- [ ] 未使用 AI
- [ ] 网页版 AI（如 ChatGPT、Claude、Kimi、豆包等）
- [x] AI Agent / 编程代理（如 Claude Code、Codex、OpenCode、Cursor Agent 等）
- [ ] 国产大模型服务（如 DeepSeek、GLM、通义千问、文心一言等）
- [ ] IDE 插件或代码补全工具（如 GitHub Copilot、Cursor、CodeGeeX 等）
- [ ] 其他：

**具体工具名称**：CodeBuddy（AI 编程助手，支持多模态交互）

**AI 主要用于哪些环节**：

| 环节 | 具体用途 |
|------|----------|
| 项目搭建 | 基于需求分析生成项目架构、依赖配置、文件结构 |
| 代码生成 | 全部 7 个 Screen 页面、4 个通用组件、导航、ViewModel、Repository、DAO、DataStore、Theme 等代码的主体生成 |
| 调试修复 | 类型不匹配（String vs Date、Double vs Float）、缺少 import、字段名错误（duration vs durationMinutes、selectedQuality vs selectedQualityFilter）等编译错误的快速定位与修复 |
| UI 迭代 | 底部导航从 2Tab 扩展为 4Tab、分析页从静态变动态、筛选标签从不可点击变可交互、深色模式从局部变全局等多个迭代 |
| 功能增强 | 闹钟添加编辑删除对话框、数据备份/关于我们/评分对话框交互、Canvas 柱状图绑定真实数据 |
| 报告整理 | 基于项目实际代码撰写本实验报告 |

## 10. 运行说明

- **最低 Android 版本**：API 24（Android 7.0）
- **推荐 Android 版本**：API 36（Android 14）
- **特殊权限**：
  - `android.permission.INTERNET`（网络权限，为后续云端功能预留）
  - `android.permission.ACCESS_NETWORK_STATE`（网络状态检测）
- **运行步骤**：
  1. 克隆仓库：`git clone https://github.com/szw200418/2025003028-FinalProject
)
  2. 使用 Android Studio（Hedgehog 及以上版本）打开项目
  3. 等待 Gradle 同步完成（首次同步需下载依赖）
  4. 连接模拟器（推荐 Pixel 6 API 36）或真机，点击 Run
  5. 首次启动会自动填充 7 条样本数据用于体验

## 11. 项目亮点

1. **Material You 深蓝紫夜空主题**：自定义 Light/Dark 双色方案，以蓝紫渐变模拟夜空氛围，契合睡眠追踪的产品调性；Material3 动态颜色支持适配 Android 12+
2. **完整的离线优先体验**：所有核心功能（记录、分析、闹钟）均不依赖网络，数据通过 Room 本地存储，DataStore 持久化偏好，0 网络请求即可完整使用
3. **动态数据分析引擎**：分析页面全部指标（质量分数、时长、入睡/起床时间、趋势判断、建议文本）均基于真实用户数据实时动态计算，而非静态占位文本
4. **Canvas 原生图表**：不引入第三方图表库，纯 Compose Canvas 手绘柱状图，轻量高性能
5. **多维度筛选系统**：支持时间维度（本周/本月）+ 质量维度（优眠/失眠）+ 收藏 的多维度组合筛选
6. **种子数据预填充**：首次安装自动插入 7 条真实感样本数据，覆盖不同质量等级和场景，方便开箱即用的体验演示
7. **跨天时长智能计算**：自动处理凌晨入睡 → 上午起床的跨天场景，时长计算准确无误

## 12. 未来改进方向

1. **云端同步与多设备**：接入 Firebase Firestore 或自有后端 API，实现睡眠记录的云端备份与多端同步
2. **智能闹钟**：集成 Android AlarmManager 实现真正的系统闹钟唤醒，根据睡眠周期（浅睡期）智能唤醒
3. **可穿戴设备集成**：接入 Health Connect API 或智能手环 SDK，自动采集心率、血氧、体动等生理数据
4. **AI 睡眠教练**：基于长期数据训练轻量模型，提供个性化的作息优化建议
5. **社交功能**：睡眠挑战、好友睡眠排行、睡眠打卡等轻社交激励
6. **推送通知**：定时提醒准备入睡、起床打卡、每周睡眠报告推送
7. **Widget 小组件**：桌面小组件展示昨晚睡眠评分和本周趋势
8. **数据导出**：支持导出 CSV/PDF 格式的睡眠报告
