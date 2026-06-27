# 图书馆管理系统

基于 **Spring Boot 2.7 + Vue 3 + MySQL** 的图书借阅管理平台，部署在 Apache Tomcat 9 上运行。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3、Vite 5、Element Plus、Pinia、Axios、ECharts |
| 后端 | Spring Boot 2.7.18、Spring Security、MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.x（utf8mb4） |
| 服务器 | Apache Tomcat 9（WAR 部署） |

## 功能概览

- **图书管理**：图书增删改查、分类管理、珍稀图书标记、图书封面上传
- **读者管理**：读者注册、读者类型配置（可借数量/天数/续借次数）
- **借阅管理**：借书、还书、续借、借阅记录查询
- **预约管理**：图书预约、座位预约
- **罚款管理**：逾期罚款自动计算与缴费
- **公告管理**：系统公告发布与展示
- **数据报表**：借阅统计、馆藏分析等可视化图表
- **系统管理**：管理员账号、角色权限、操作日志、系统配置
- **门户首页**：面向读者的图书检索与信息展示

## 项目结构

```
├── frontend/              # 前端 Vue 项目
│   ├── src/
│   │   ├── api/           # 接口请求
│   │   ├── router/        # 路由配置
│   │   ├── store/         # Pinia 状态管理
│   │   └── views/         # 页面组件
│   └── package.json
├── src/main/java/com/library/
│   ├── config/            # Spring 配置（Security、MyBatis 等）
│   ├── controller/        # 控制器
│   ├── entity/            # 实体类
│   ├── mapper/            # MyBatis Mapper 接口
│   ├── service/           # 业务逻辑层
│   └── util/              # 工具类
├── src/main/resources/
│   ├── application.yml    # 应用配置
│   ├── mapper/            # MyBatis XML 映射文件
│   ├── schema.sql         # 建表脚本
│   ├── data.sql           # 初始数据
│   └── static/            # 前端构建产物（npm run build 后生成）
├── sql/                   # 数据库脚本备份
├── pom.xml                # Maven 配置
├── deploy.bat             # 一键部署脚本（已有 Tomcat）
└── setup-deploy.bat       # 自动环境配置 + 部署脚本
```

## 环境要求

| 依赖 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 16+（含 npm） |
| MySQL | 8.0+ |
| Tomcat | 9.x |

## 快速开始

### 1. 初始化数据库

启动 MySQL，执行建表和初始数据脚本：

```sql
source sql/schema.sql;
source sql/data.sql;
```

### 2. 修改数据库连接

编辑 `src/main/resources/application.yml`，修改数据库用户名和密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db?...
    username: root
    password: 你的密码
```

### 3. 部署项目

根据你的环境情况，选择以下任一脚本：

---

## 脚本使用说明

### `setup-deploy.bat` — 首次部署 / 没有 Tomcat

适合**首次部署**或**尚未安装 Tomcat** 的场景。

双击运行后会依次完成：

```
[1/6] 检查运行环境   — 自动检测 Java、Maven、Node.js，缺失则提示下载地址
[2/6] 配置 Tomcat    — 询问是否已安装 Tomcat
                      ├─ 没有 → 自动从 Apache 官网下载 Tomcat 9 到项目 tomcat/ 目录
                      └─ 有   → 输入路径或选择自动搜索到的路径
[3/6] 构建前端       — npm run build
[4/6] Maven 打包     — mvn clean package -DskipTests
[5/6] 部署到 Tomcat  — 停止旧实例、替换 WAR 文件
[6/6] 启动 Tomcat    — 启动服务，访问 http://localhost:8080/library
```

Tomcat 路径会保存到 `tomcat-path.txt`，下次运行自动读取，无需重复配置。

---

### `deploy.bat` — 日常更新部署

适合**已配置好环境、需要快速更新项目**的场景。

双击运行后会：

```
1. 询问 Tomcat 路径   — 显示当前默认路径，回车使用默认，或输入新路径
2. 构建前端           — npm run build
3. Maven 打包         — mvn clean package -DskipTests
4. 停止 Tomcat        — 结束正在运行的实例
5. 替换 WAR           — 删除旧部署，复制新 WAR
6. 启动 Tomcat        — 启动服务
```

**修改默认 Tomcat 路径**：用文本编辑器打开 `deploy.bat`，修改第 8 行：

```bat
if not defined CATALINA_HOME set "CATALINA_HOME=你的Tomcat路径"
```

---

### 手动部署（不使用脚本）

```bash
# 1. 构建前端
cd frontend
npm run build
cd ..

# 2. Maven 打包
mvn clean package -DskipTests

# 3. 将 target/library.war 复制到 Tomcat 的 webapps 目录

# 4. 重启 Tomcat
```

## 访问地址

| 角色 | 地址 |
|------|------|
| 管理后台 | http://localhost:8080/library/admin |
| 读者门户 | http://localhost:8080/library |

## 常见问题

**Q：部署后页面空白或 404？**
A：确认前端已构建（`frontend/dist` 内容已复制到 `src/main/resources/static`），且 WAR 文件已正确解压。

**Q：数据库连接失败？**
A：检查 `application.yml` 中的数据库地址、用户名、密码是否正确，确认 MySQL 服务已启动。

**Q：Tomcat 端口冲突？**
A：编辑 `Tomcat安装目录/conf/server.xml`，修改 `<Connector port="8080">` 为其他端口。

**Q：脚本提示 `mvn` 或 `npm` 不是内部命令？**
A：需要将 Maven 和 Node.js 添加到系统环境变量 `PATH` 中。
