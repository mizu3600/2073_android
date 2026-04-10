# IM2073 Clicker System

基于 IM2073 作业要求构建的完整 Clicker System，包含：

- Android 客户端：学生点击 A/B/C/D 按钮提交投票
- Java Servlet 后端：接收投票并写入 MySQL，展示统计页面
- MySQL 脚本：初始化 `clicker_system.responses` 表
- Tomcat 部署脚本：将 WAR 包部署到本机 Tomcat

## 目录

- `android-client/`：Android Studio / Gradle 项目
- `server/`：Java Servlet + Maven WAR 项目
- `database/`：数据库初始化与查询脚本
- `scripts/`：构建、部署、环境脚本

## 当前结构

- `android-client/app/src/main/java/com/example/clicker/`：Android Activity 与 HTTP 请求
- `server/src/main/java/com/example/clicker/server/`：Servlet、数据库访问与应用清理逻辑
- `database/`：数据库初始化与查询脚本
- `scripts/`：本地构建与部署脚本

## 本机环境

- Android SDK：`~/Library/Android/sdk`
- Android Studio JBR：`/Applications/Android Studio.app/Contents/jbr/Contents/Home`
- MySQL：本机已运行
- Tomcat：`/opt/homebrew/opt/tomcat`

## 快速运行

```bash
cd /Users/jesse/Desktop/2073_android
bash scripts/setup_mysql.sh
bash scripts/build_server.sh
bash scripts/deploy_tomcat.sh
bash scripts/build_android.sh
```

## 访问地址

- Lecturer 页面：`http://localhost:8080/clicker/display`
- Servlet 测试：`http://localhost:8080/clicker/select?choice=a`
- 状态接口：`http://localhost:8080/clicker/status?questionNo=1`
- Lecturer 控制：页面内 `Start Poll` / `End Now` 按钮

## Android 地址说明

- 模拟器默认使用：`http://10.0.2.2:8080/clicker/select`
- 真机调试时，请在 [`gradle.properties`](/Users/jesse/Desktop/2073_android/android-client/gradle.properties) 中设置 `SERVER_BASE_URL=http://你的电脑局域网IP:8080/clicker/select`

## 时间窗口

- 每道题的开放时间保存在 MySQL `questions` 表
- 默认初始化为：`2026-01-01 00:00:00` 到 `2026-12-31 23:59:59`
- 超过结束时间或尚未到开始时间时，后端会拒绝投票和评论
- Lecturer 页面可以直接点击 `Start Poll` 并选择时长，或点击 `End Now` 立即结束
- Android 会通过 `/status` 读取当前状态，并在关闭时禁用投票与评论

## 说明

Tomcat 11 使用 `jakarta.servlet.*` 包名，因此后端代码采用 Jakarta Servlet API。课程演示逻辑与传统 `HttpServlet` 一致。
