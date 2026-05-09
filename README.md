# 印刷行业综合管理系统

基于 Spring Boot 3.x + Vue 3 的印刷行业后台管理系统。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 (Java 21) |
| 安全框架 | Spring Security 6.x + JWT |
| ORM | MyBatis Plus 3.5.7 |
| 数据库 | MySQL 8.0 |
| 前端框架 | Vue 3 + TypeScript |
| UI 组件 | Element Plus |
| 数据可视化 | ECharts |
| 构建工具 | Vite 5 + Maven |

## 功能模块

- **用户与权限** — JWT 认证、RBAC 角色权限、菜单管理、操作日志
- **订单管理** — 订单 CRUD、多条件搜索、Excel 导入导出、送货单打印
- **客户管理** — 客户 CRUD、Excel 导入导出、智能关联订单
- **刀模管理** — 刀模 CRUD、型号自动生成、位置编码管理、标签打印
- **数据统计** — 仪表盘、订单趋势、客户排行

## 快速开始

### 环境要求

- JDK 21（必须使用 JDK 21，JDK 26 与 Lombok 不兼容）
- Node.js 18+
- MySQL 8.0
- Maven（使用项目自带的 Maven Wrapper，无需预装）

### 1. 初始化数据库

```bash
mysql -u root -p --default-character-set=utf8mb4 -e "SOURCE sql/01-schema.sql"
mysql -u root -p --default-character-set=utf8mb4 -D print_management -e "SOURCE sql/02-seed.sql"
```

### 2. 启动后端

```bash
cd print-backend
set JAVA_HOME=D:\IDEA\JDK\jdk-21.0.11
mvnw.cmd spring-boot:run
```

后端运行在 `http://localhost:8080`

### 3. 启动前端

```bash
cd print-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，自动代理 `/api` 到后端 8080 端口。

### 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 项目结构

```
printProject/
├── print-backend/          # Spring Boot 后端
│   ├── src/main/java/com/print/
│   │   ├── common/         # 统一返回、异常处理、工具类
│   │   ├── config/         # MyBatis Plus、Jackson 配置
│   │   ├── security/       # Spring Security + JWT
│   │   ├── annotation/     # @OperateLog 注解
│   │   ├── aspect/         # 操作日志 AOP 切面
│   │   └── module/         # 业务模块
│   │       ├── auth/       # 登录/注册
│   │       ├── sys/        # 用户/角色/菜单/日志管理
│   │       ├── customer/   # 客户管理
│   │       ├── mold/       # 刀模管理
│   │       ├── order/      # 订单管理
│   │       └── dashboard/  # 数据统计
│   └── src/main/resources/
│       ├── application.yml
│       └── mapper/         # MyBatis XML
│
├── print-frontend/         # Vue 3 前端
│   ├── src/
│   │   ├── api/            # Axios 请求封装
│   │   ├── views/          # 页面组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # Pinia 状态管理
│   │   └── layout/         # 布局组件
│   └── vite.config.ts
│
└── sql/                    # 数据库脚本
    ├── 01-schema.sql       # 建表语句（9张表）
    └── 02-seed.sql         # 初始数据（3个角色 + admin 用户 + 菜单权限）
```
