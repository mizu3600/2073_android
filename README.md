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

- `android-client/app/src/main/java/com/example/clicker/ui/`：Activity 与界面交互
- `android-client/app/src/main/java/com/example/clicker/network/`：客户端 HTTP 请求
- `android-client/app/src/main/java/com/example/clicker/config/`：客户端运行配置
- `server/src/main/java/com/example/clicker/server/web/`：Servlet 与页面渲染
- `server/src/main/java/com/example/clicker/server/service/`：投票业务逻辑
- `server/src/main/java/com/example/clicker/server/repository/`：JDBC 数据访问
- `server/src/main/java/com/example/clicker/server/config/`：数据库连接与应用清理逻辑

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

## Android 地址说明

- 模拟器默认使用：`http://10.0.2.2:8080/clicker/select`
- 真机调试时，请在 [`gradle.properties`](/Users/jesse/Desktop/2073_android/android-client/gradle.properties) 中设置 `SERVER_BASE_URL=http://你的电脑局域网IP:8080/clicker/select`

## 说明

Tomcat 11 使用 `jakarta.servlet.*` 包名，因此后端代码采用 Jakarta Servlet API。课程演示逻辑与传统 `HttpServlet` 一致。
