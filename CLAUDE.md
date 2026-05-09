# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

### Backend (Spring Boot 3.2.5, Java 21)
```bash
cd print-backend
# Set JDK 21 (JDK 26 breaks Lombok)
export JAVA_HOME="D:/IDEA/JDK/jdk-21.0.11"
# Compile only
./mvnw.cmd compile
# Run
./mvnw.cmd spring-boot:run
# Package as JAR
./mvnw.cmd package -DskipTests
```

### Frontend (Vue 3 + Vite, Node 24)
```bash
cd print-frontend
npm install       # after clone
npm run dev       # dev server at localhost:5173
npm run build     # production build to dist/
```

### Database (MySQL 8.0)
- Service: `MySQL80` (Windows service, auto-start)
- Root password: `boss.1234`
- Schema + seed: `mysql -u root -pboss.1234 --default-character-set=utf8mb4 -D print_management -e "SOURCE sql/01-schema.sql; SOURCE sql/02-seed.sql;"`
- Login credentials: `admin` / `admin123`

### Git Remote
```bash
git remote add origin https://github.com/zhuyongze1/printProject.git
git push -u origin master
```

## Project Architecture

Monorepo with two top-level projects:

```
printProject/
├── print-backend/      # Spring Boot REST API
├── print-frontend/     # Vue 3 SPA
└── sql/                # Database scripts
```

## Project Architecture

Monorepo with two top-level projects:

```
printProject/
├── print-backend/      # Spring Boot REST API
├── print-frontend/     # Vue 3 SPA
└── sql/                # Database scripts
```

### Backend Structure
```
com.print
├── PrintApplication.java     # Entry point
├── common/                   # Shared utilities
│   ├── result/               # Result.java, PageResult.java (unified JSON response)
│   ├── exception/            # BusinessException, GlobalExceptionHandler
│   └── util/                 # JwtUtil, SecurityUtil
├── config/                   # MyBatisPlus, Jackson, MetaObjectHandler
├── security/                 # JwtAuthFilter, SecurityConfig (Spring Security 6)
├── annotation/ + aspect/     # @OperateLog AOP for audit logging
└── module/
    ├── auth/                 # Login/Register (JWT generation)
    ├── sys/                  # System management (user/role/menu/log CRUD)
    ├── customer/             # Customer CRUD
    ├── mold/                 # Knife mold CRUD + model/location code generation
    ├── order/                # Order CRUD (smart inline customer/mold creation)
    └── dashboard/            # Statistics aggregation
```

Key patterns:
- **Layers**: Controller → Service → Mapper (MyBatis Plus `BaseMapper<T>`)
- **DTO separation**: Controllers never return entities directly (Entity != DTO)
- **Soft delete**: All tables have `is_deleted` with `@TableLogic`
- **RBAC**: `@PreAuthorize("hasAuthority('order:list')")` on controller methods
- **Unified response**: `Result.success(data)` / `Result.error(code, msg)` with `{code, msg, data, timestamp}`
- **Constructor injection** (no `@Autowired`)

### Frontend Structure
```
src/
├── api/           # Axios instances per module (auth, user, role, menu, log, customer, mold, order, dashboard)
├── assets/styles/ # global.scss (Apple-style CSS variables)
├── layout/        # MainLayout.vue (sidebar + navbar)
├── router/        # Vue Router with permission guard skeleton
├── stores/        # Pinia (user store with token/permissions)
└── views/         # Pages: login, dashboard, order, customer, mold, statistics, system/*
```

Key patterns:
- Composition API with `<script setup lang="ts">`
- Element Plus components with Apple-style CSS overrides
- Axios interceptor auto-injects `Bearer` token, handles 401 redirect
- Vite proxies `/api` → `localhost:8080` in dev mode

## Dev Environment

| Tool | Path |
|------|------|
| JDK 21 | `D:\IDEA\JDK\jdk-21.0.11` |
| Node | v24.15.0 (system) |
| Maven | Maven Wrapper (`mvnw.cmd`, auto-downloads 3.9.6) |
| MySQL | 8.0 Windows service (port 3306) |

**Important**: Always use JDK 21 (not 26) for Maven builds — Lombok 1.18.30 is incompatible with JDK 26.

## Key Technical Decisions

- **Excel**: EasyExcel (com.alibaba) — annotation-driven, streaming read/write
- **PDF**: OpenPDF (LGPL) + ZXing (QR codes) — for delivery notes and mold labels
- **JWT**: io.jsonwebtoken (jjwt 0.12.5) — 24h expiration, no Redis blacklist (v1)
- **Redis**: Skipped for v1 (token blacklist not needed; logout is client-side)
- **MySQL**: All IDs use `BIGINT AUTO_INCREMENT` (not UUID/snowflake)
- **Number format**: `ORD-YYYYMMDD-NNNN`, `CUS-YYYYMMDD-NNNN`, `DM-YYYYMMDD-NNNN`
- **Menu/Permission**: Unified `sys_menu` with `menu_type` (0=directory, 1=menu, 2=button)
- **Operation Log**: `@OperateLog` annotation + AOP aspect (auto-records on annotated methods)

## TODO (Not Yet Implemented)

- Excel import/export endpoints (EasyExcel dependency included, controller stubs exist)
- PDF delivery note generation (OpenPDF + customer isolation validation)
- PDF mold label generation (including QR code via ZXing)
- Frontend route permission guard (dynamic menu filtering by user permissions)
- Frontend button-level permission (`v-if="hasPermission('order:create')"`)
- Redis integration (for v2: token blacklist, rate limiting, cache)

## Known Issues & Fixes (2026-05-09)

### KnifeMold entity status type mismatch
- `KnifeMold.java` line 25: `private Integer status;` should be `private String status;`
- DB column is `VARCHAR(20)`, entity had `Integer`, causing deserialization failure on create
- **Fix**: Changed to `String`

### Mold location code null safety
- `MoldService.generateLocationCode()` was calling `Integer.parseInt()` on potentially null `shelfNo`/`layerNo`/`positionNo`
- **Fix**: Added null check fallback

### GitHub language detection
- `.agents/` directory (Claude Code skills) contained Python files that skewed GitHub's language stats
- **Fix**: Added `.agents/` to `.gitignore`, removed from git tracking, added `.gitattributes`

### Curl testing with Chinese characters
- On Windows bash, `curl -d '{"name":"中文"}'` sends GBK encoding, not UTF-8
- Backend returns `Invalid UTF-8 start byte` error
- **Fix**: Use `--data-binary @file.json` with UTF-8 encoded file, or use ASCII-only test data

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `Port 8080 already in use` | `cmd /c "netstat -ano \| findstr :8080 \| findstr LISTENING"` → find PID → `cmd /c "taskkill /F /PID <PID>"` |
| `maven.multiModuleProjectDirectory` error | Must use JDK 21, not 26. Re-run with `JAVA_HOME=D:\IDEA\JDK\jdk-21.0.11` |
| MySQL `mysql` command not found | Use full path: `"C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"` |
| Frontend proxy not working | Check Vite config: `server.proxy` must point to `http://localhost:8080` |
| Entity field type mismatch | Check DB column type vs Java entity field type (especially String vs Integer for status/enum fields) |
