# IM2073 Clicker System

一个完整的课堂 Clicker System 示例项目，包含 Android 学生端、Java Servlet 服务端、MySQL 数据库脚本，以及本地构建和部署脚本。

当前仓库中的实现不是一个单独的“投票接口”样例，而是一套可以本地跑通的完整链路：

- Android 端负责展示题目、轮询投票状态、提交投票、提交匿名评论
- Servlet 服务端负责校验投票窗口、写入数据库、提供状态接口、渲染 Lecturer 页面
- MySQL 负责保存题目配置、投票记录、评论记录
- Tomcat 负责承载 WAR 包并对外提供 HTTP 接口

## 项目概览

系统主要由 4 个目录组成：

- `android-client/`
  Android Studio / Gradle 项目，面向学生端
- `server/`
  Java 17 + Maven + Jakarta Servlet WAR 项目，面向服务端
- `database/`
  MySQL 建库建表脚本与查询示例
- `scripts/`
  本机构建、导库、部署脚本

## 仓库结构

下面的结构基于当前仓库实际文件整理，省略了部分构建产物和工具缓存目录：

```text
2073_android/
├── README.md
├── android-client/
│   ├── build.gradle
│   ├── gradle.properties
│   ├── settings.gradle
│   ├── gradlew
│   ├── gradlew.bat
│   ├── gradle/
│   │   └── wrapper/
│   │       ├── gradle-wrapper.jar
│   │       └── gradle-wrapper.properties
│   └── app/
│       ├── build.gradle
│       ├── proguard-rules.pro
│       └── src/main/
│           ├── AndroidManifest.xml
│           ├── java/com/example/clicker/
│           │   ├── MainActivity.java
│           │   ├── VoteApiClient.java
│           │   ├── CommentApiClient.java
│           │   ├── StatusApiClient.java
│           │   └── PollStatusResponse.java
│           └── res/
│               ├── layout/activity_main.xml
│               ├── values/
│               │   ├── strings.xml
│               │   ├── colors.xml
│               │   └── themes.xml
│               └── drawable/
│                   ├── bg_choice_a.xml
│                   ├── bg_choice_b.xml
│                   ├── bg_choice_c.xml
│                   ├── bg_choice_d.xml
│                   ├── bg_comment_button.xml
│                   ├── bg_comment_input.xml
│                   ├── bg_header_card.xml
│                   ├── bg_info_card.xml
│                   ├── bg_question_chip.xml
│                   ├── bg_response_card.xml
│                   └── bg_screen.xml
├── server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/clicker/server/
│       │   ├── AppCleanupListener.java
│       │   ├── CommentEntry.java
│       │   ├── CommentServlet.java
│       │   ├── DatabaseHelper.java
│       │   ├── DisplayServlet.java
│       │   ├── PollControlServlet.java
│       │   ├── PollStatus.java
│       │   ├── QuestionSchedule.java
│       │   ├── SelectServlet.java
│       │   └── StatusServlet.java
│       ├── resources/
│       │   └── db.properties
│       └── webapp/
│           ├── index.html
│           └── WEB-INF/web.xml
├── database/
│   ├── clicker_dump.sql
│   └── query_examples.sql
└── scripts/
    ├── build_android.sh
    ├── build_server.sh
    ├── deploy_tomcat.sh
    ├── env.sh
    └── setup_mysql.sh
```

说明：

- `android-client/build/`、`server/target/`、`android-client/.gradle/` 等目录属于构建产物或本地缓存，不是核心源代码
- `server/target/clicker.war` 会在执行 Maven 打包后生成，用于部署到 Tomcat

## 系统架构

### 1. Android 学生端

学生端目前是一个单 Activity 应用：

- `MainActivity.java`
  负责界面初始化、按钮点击、状态轮询、启用和禁用输入控件
- `VoteApiClient.java`
  通过 `GET /select` 提交 A/B/C/D 投票
- `CommentApiClient.java`
  通过 `POST /comment` 提交匿名评论
- `StatusApiClient.java`
  通过 `GET /status` 轮询当前题目的开放状态
- `PollStatusResponse.java`
  对 `/status` 返回的 JSON 做简单封装

当前 Android 端的特点：

- 只有一个页面，没有 Fragment、ViewModel、Repository、Room
- 没有本地数据库，也没有离线缓存
- 题号当前固定为 `QUESTION_NO = 1`
- 每 5 秒轮询一次服务端状态
- 当投票未开始或已经结束时，按钮和评论输入框会被禁用

### 2. Servlet 服务端

服务端是一个部署到 Tomcat 的 WAR 应用，使用 Jakarta Servlet API。

核心类职责如下：

- `DatabaseHelper.java`
  全部数据库访问都集中在这里，包括查题目、写投票、写评论、统计票数、读取最新评论、开始和结束投票
- `SelectServlet.java`
  投票接口，接收 `choice` 和 `questionNo`
- `CommentServlet.java`
  评论接口，只允许 `POST`
- `StatusServlet.java`
  返回当前题目的 JSON 状态，供 Android 轮询
- `DisplayServlet.java`
  生成 Lecturer 页面，包括图表、统计摘要、评论列表、控制按钮
- `PollControlServlet.java`
  处理 Lecturer 页面上的开始和结束投票操作
- `QuestionSchedule.java`
  表示题目时间窗口，并判断当前状态是 `NOT_STARTED`、`OPEN` 还是 `CLOSED`
- `PollStatus.java`
  状态枚举
- `CommentEntry.java`
  评论模型对象
- `AppCleanupListener.java`
  Web 应用卸载时清理 JDBC Driver 和 MySQL 清理线程，避免 Tomcat 热部署后的资源泄漏

### 3. Lecturer 页面

Lecturer 页面由 `DisplayServlet.java` 直接拼接 HTML 输出，不依赖 JSP 或前端框架。

页面包含这些区域：

- 当前题目与时间状态
- 总票数、领先选项、评论数、剩余时间
- Bar Chart、Column Chart、Pie Chart 三种可切换图表
- Vote Breakdown 表格
- Recent Comments 列表
- Start Poll / End Poll 控制区

也就是说，老师端前端和服务端渲染逻辑完全在一个 Servlet 文件中。

### 4. 数据库

数据库名称为：

- `clicker_system`

业务表总共有 3 张：

#### `questions`

保存题目配置和开放时间窗口。

字段：

- `questionNo`：题号，主键
- `questionText`：题目文本
- `startTime`：开始时间
- `endTime`：结束时间

作用：

- 决定题目什么时候开始接收投票和评论
- 决定 Android `/status` 的返回状态
- 决定 Lecturer 页面显示的当前题目和时间信息
- 被 `PollControlServlet` 更新，以实现开始和结束投票

#### `responses`

保存学生投票记录。

字段：

- `questionNo`：题号
- `choice`：学生选项，当前设计为 `a`、`b`、`c`、`d`

作用：

- 每次投票都会插入一行
- Lecturer 页面会基于这张表做按选项聚合统计

说明：

- 当前表结构没有主键、没有学生 ID、没有去重逻辑
- 这意味着同一个设备或同一个人理论上可以多次提交投票，数据库会累计记录

#### `comments`

保存匿名评论。

字段：

- `id`：自增主键
- `questionNo`：题号
- `commentText`：评论内容，最大 240 字
- `createdAt`：创建时间，默认当前时间戳

作用：

- 学生端提交匿名评论时写入
- Lecturer 页面读取最新 10 条评论展示

#### 索引

当前还创建了 2 个索引：

- `idx_question_choice`：`responses(questionNo, choice)`
- `idx_comments_question_created`：`comments(questionNo, createdAt)`

它们主要用于：

- 提升按题目统计投票时的查询效率
- 提升按题目读取最新评论时的查询效率

## 默认数据

数据库初始化脚本会默认插入 1 道题：

- `questionNo = 1`
- `questionText = 'Who is the coolest Marvel Hero?'`
- `startTime = 2026-01-01 00:00:00`
- `endTime = 2026-12-31 23:59:59`

这也和当前 Android 端硬编码的 `QUESTION_NO = 1` 对应。

## 接口列表

当前服务端通过注解暴露了以下接口：

### `GET /clicker/select`

学生提交投票接口。

请求参数：

- `questionNo`
- `choice`

示例：

```text
http://localhost:8080/clicker/select?questionNo=1&choice=a
```

行为：

- 校验选项只能是 `a/b/c/d`
- 校验题目是否存在
- 校验当前时间是否处于题目开放窗口
- 校验通过后写入 `responses`

### `POST /clicker/comment`

学生提交匿名评论接口。

表单参数：

- `questionNo`
- `comment`

行为：

- 只允许 `POST`
- 评论不能为空
- 评论长度不能超过 240
- 必须在题目开放窗口内
- 校验通过后写入 `comments`

### `GET /clicker/status`

Android 轮询状态接口。

请求参数：

- `questionNo`

返回内容包含：

- `success`
- `questionNo`
- `questionText`
- `status`
- `acceptingResponses`
- `message`
- `startTime`
- `endTime`
- `serverTime`

### `GET /clicker/display`

Lecturer 结果展示页面。

请求参数：

- `questionNo`
- `notice`，通常由 `/control` 重定向带回

页面功能：

- 查看票数统计
- 查看图表
- 查看最新评论
- 开始投票
- 结束投票

### `POST /clicker/control`

Lecturer 控制接口。

表单参数：

- `questionNo`
- `action=start|end`
- `durationSeconds`，仅 `start` 时使用

行为：

- `start`：把 `startTime` 设为当前时间，`endTime` 设为当前时间加上持续时长
- `end`：把 `endTime` 设为当前时间
- 操作完成后重定向回 `/display`

## 构建与部署

### 本机环境

当前脚本假定的默认环境如下：

- Android SDK：`~/Library/Android/sdk`
- Java：`/Applications/Android Studio.app/Contents/jbr/Contents/Home`
- Tomcat：`/opt/homebrew/opt/tomcat`
- MySQL：本机可执行 `mysql` 命令且服务已启动

相关默认值定义在：

- `scripts/env.sh`

### 一键执行顺序

在项目根目录执行：

```bash
cd /Users/jesse/Desktop/2073_android
bash scripts/setup_mysql.sh
bash scripts/build_server.sh
bash scripts/deploy_tomcat.sh
bash scripts/build_android.sh
```

### 各脚本作用

#### `scripts/setup_mysql.sh`

作用：

- 使用 `mysql` 命令导入 `database/clicker_dump.sql`
- 初始化数据库 `clicker_system`
- 创建 `questions`、`responses`、`comments` 三张表

可用环境变量：

- `MYSQL_USER`
- `MYSQL_PASSWORD`

#### `scripts/build_server.sh`

作用：

- 进入 `server/`
- 执行 `mvn clean package`
- 生成 `server/target/clicker.war`

#### `scripts/deploy_tomcat.sh`

作用：

- 先调用 `build_server.sh`
- 把 `clicker.war` 复制到 Tomcat `webapps`
- 重启本机 Tomcat

部署完成后访问：

```text
http://localhost:8080/clicker/display
```

#### `scripts/build_android.sh`

作用：

- 进入 `android-client/`
- 执行 `./gradlew clean assembleDebug`

#### `scripts/env.sh`

作用：

- 统一设置 `JAVA_HOME`
- 统一设置 `ANDROID_SDK_ROOT`
- 统一设置 `TOMCAT_HOME`
- 把相关工具目录加入 `PATH`

## Android 地址配置

Android 模拟器默认使用：

```text
http://10.0.2.2:8080/clicker/select
```

该地址定义逻辑位于：

- `android-client/app/build.gradle`

构建时会基于这个地址自动推导：

- 投票接口：`SERVER_BASE_URL`
- 评论接口：`SERVER_COMMENT_URL`
- 状态接口：`SERVER_STATUS_URL`

如果用真机调试，请在 `android-client/gradle.properties` 中覆盖：

```properties
SERVER_BASE_URL=http://你的电脑局域网IP:8080/clicker/select
```

例如：

```properties
SERVER_BASE_URL=http://192.168.1.10:8080/clicker/select
```

## 页面与交互逻辑

### Android 学生端界面

`activity_main.xml` 当前包含：

- 题目标题 `txtQuestion`
- 当前状态文本 `txtPollStatus`
- 四个投票按钮 `btnA`、`btnB`、`btnC`、`btnD`
- 评论标签 `txtCommentLabel`
- 评论输入框 `edtComment`
- 评论提交按钮 `btnCommentSubmit`

`MainActivity.java` 的主要流程：

1. 启动后先禁用输入控件
2. 请求 `/status`
3. 根据服务端返回决定是否允许投票和评论
4. 每 5 秒重复轮询一次状态
5. 点击 A/B/C/D 按钮时提交投票
6. 点击评论按钮时提交匿名评论

### Lecturer 页面交互

`DisplayServlet.java` 负责：

- 读取题目时间窗口
- 聚合投票数量
- 读取最近 10 条评论
- 渲染统计摘要
- 渲染图表和表格
- 根据当前状态显示 Start Poll 或 End Poll 控件
- 在投票进行中显示倒计时

## 数据文件说明

### `database/clicker_dump.sql`

作用：

- 创建数据库
- 删除旧表
- 创建新表
- 创建索引
- 插入默认题目

### `database/query_examples.sql`

作用：

- 展示常见查询写法
- 提供一些测试插入语句
- 方便手动验证数据库中的票数和评论数据

包含的示例操作有：

- 查询题目
- 更新题目时间窗口
- 手动插入测试投票
- 手动插入测试评论
- 按选项汇总投票
- 查询最新评论

## 当前实现特征与限制

基于当前代码，项目有以下实现特征：

- 当前默认只围绕题号 `1` 工作
- Android 端没有登录、用户识别或去重投票机制
- 投票记录表没有学生标识，因此无法限制一人一票
- Lecturer 页面是 Servlet 直接拼 HTML，不是模板引擎或前后端分离
- 服务端使用原生 JDBC，不是 Spring Boot、JPA 或 MyBatis
- 评论是匿名评论，不存用户名
- 评论和投票都依赖 `questions.startTime/endTime` 进行开放性校验

这些都是当前项目实现的一部分，不是文档遗漏。

## 常用访问地址

- Lecturer 页面：
  `http://localhost:8080/clicker/display`
- 测试投票接口：
  `http://localhost:8080/clicker/select?choice=a`
- 状态接口：
  `http://localhost:8080/clicker/status?questionNo=1`
- 默认欢迎页：
  `http://localhost:8080/clicker/`

## 技术栈

- Android 原生应用
- Gradle
- Java 17
- Maven
- Jakarta Servlet API
- MySQL Connector/J
- MySQL
- Tomcat 11

## 备注

当前仓库中的实际数据库脚本定义了 3 张业务表：

- `questions`
- `responses`
- `comments`

如果你之前只把它理解成“只有一个 `responses` 表的投票作业”，那已经和当前代码实现不一致了。现在这个仓库实际上已经扩展成：

- 带题目开放时间控制
- 带 Lecturer 管理页面
- 带匿名评论
- 带状态轮询

的一套完整 Clicker System。
