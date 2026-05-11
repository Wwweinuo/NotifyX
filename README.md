# NotifyX - 生日/节日邮件提醒系统

一个基于 Spring Boot + Vue 3 的全栈提醒系统，自动在生日和节日前发送邮件提醒，支持公历、农历及动态规则节日。

## 功能特性

- **联系人管理**：支持公历/农历生日，农历自动转换为公历日期
- **节日管理**：支持固定公历、固定农历、动态规则（如母亲节=5月第2个星期日）三种类型
- **邮件提醒**：定时检查并自动发送提醒邮件，支持自定义发件人名称（中文编码正确）
- **定时任务**：Cron 表达式可在线动态修改，修改后立即生效无需重启
- **提醒记录**：记录每次邮件发送结果，防重复发送，失败自动重试（最多3次）

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.3 / Spring Data JPA / Spring Mail / PostgreSQL |
| 前端 | Vue 3 / TypeScript / Vite / Element Plus / Axios |
| 农历 | lunar 1.3.9（农历转公历） |

## 项目结构

```
NotifyX/
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/notifyx/
│       ├── config/             # 配置（CORS、异常处理、数据初始化）
│       ├── controller/         # REST API 控制器
│       ├── dto/                # 数据传输对象
│       ├── entity/             # JPA 实体
│       ├── enums/              # 枚举类型
│       ├── repository/         # JPA Repository
│       ├── scheduler/          # 动态定时任务调度
│       └── service/            # 业务逻辑
├── frontend/                   # Vue 3 前端
│   └── src/
│       ├── api/                # API 请求封装
│       ├── components/         # 公共组件
│       ├── layout/             # 布局组件
│       ├── router/             # 路由配置
│       ├── styles/             # 全局样式
│       └── views/              # 页面视图
└── schema.sql                  # 数据库建表脚本（参考用）
```

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.8+

### 1. 创建数据库

```sql
CREATE DATABASE notifyx;
```

### 2. 配置后端

编辑 `backend/src/main/resources/application.yml`，修改数据库连接和邮件配置：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notifyx
    username: your_username
    password: your_password
  mail:
    host: smtp.qq.com        # QQ 邮箱 SMTP
    port: 587
    username: your_email@qq.com
    password: your_auth_code  # 授权码，非登录密码
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，自动代理 `/api` 请求到后端。

### 5. 访问系统

浏览器打开 `http://localhost:5173`，进入系统后：

1. 在 **邮件配置** 页面填写 SMTP 信息并测试
2. 在 **联系人管理** 添加生日提醒对象
3. 在 **节日管理** 查看/管理内置节日
4. 在 **定时任务** 配置提醒检查时间

## API 概览

| 模块 | 接口 | 说明 |
|------|------|------|
| 联系人 | `GET/POST/PUT/DELETE /api/contacts` | CRUD + 分页搜索 |
| 节日 | `GET/POST/PUT/DELETE /api/holidays` | CRUD |
| 邮件配置 | `GET/PUT /api/email-config` | 获取/更新配置 + 测试发送 |
| 定时任务 | `GET/PUT /api/scheduler-config` | 获取/更新 Cron + 启用/禁用 |
| 提醒记录 | `GET /api/reminders` | 分页查询记录 |
| 仪表盘 | `GET /api/dashboard/stats` | 统计数据 |
| 仪表盘 | `GET /api/dashboard/upcoming` | 近7天提醒预览 |

## License

MIT
