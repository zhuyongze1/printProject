-- ============================================================
-- 印刷行业综合管理系统 - 数据库建表脚本
-- Database: print_management
-- ============================================================

CREATE DATABASE IF NOT EXISTS print_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE print_management;

-- -----------------------------------------------------------
-- 1. 系统用户表
-- -----------------------------------------------------------
CREATE TABLE sys_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(200) NOT NULL COMMENT '加密密码',
    real_name   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar      VARCHAR(200) DEFAULT NULL COMMENT '头像',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人ID',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人ID',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    UNIQUE KEY uk_username (username),
    INDEX idx_user_status (status),
    INDEX idx_user_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 系统角色表
-- -----------------------------------------------------------
CREATE TABLE sys_role (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50)  NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人ID',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人ID',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    UNIQUE KEY uk_role_code (role_code),
    INDEX idx_role_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- -----------------------------------------------------------
-- 3. 菜单/权限表（菜单和按钮权限统一管理）
-- -----------------------------------------------------------
CREATE TABLE sys_menu (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id   BIGINT       DEFAULT NULL COMMENT '父菜单ID',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单/权限名称',
    path        VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component   VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon        VARCHAR(50)  DEFAULT NULL COMMENT '图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    menu_type   TINYINT      NOT NULL COMMENT '0目录 1菜单 2按钮',
    permission  VARCHAR(100) DEFAULT NULL COMMENT '权限标识，如 order:list',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT NULL COMMENT '创建人ID',
    update_by   BIGINT       DEFAULT NULL COMMENT '更新人ID',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    INDEX idx_menu_parent (parent_id),
    INDEX idx_menu_type (menu_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单/权限表';

-- -----------------------------------------------------------
-- 4. 用户角色关联表
-- -----------------------------------------------------------
CREATE TABLE sys_user_role (
    id      BIGINT  AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT  NOT NULL COMMENT '用户ID',
    role_id BIGINT  NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- -----------------------------------------------------------
-- 5. 角色菜单关联表
-- -----------------------------------------------------------
CREATE TABLE sys_role_menu (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单/权限ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- -----------------------------------------------------------
-- 6. 操作日志表
-- -----------------------------------------------------------
CREATE TABLE sys_operation_log (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id     BIGINT       DEFAULT NULL COMMENT '操作人ID',
    username    VARCHAR(50)  DEFAULT NULL COMMENT '操作人用户名',
    operation   VARCHAR(200) NOT NULL COMMENT '操作描述',
    method      VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    params      TEXT         DEFAULT NULL COMMENT '请求参数',
    ip          VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0失败 1成功',
    error_msg   TEXT         DEFAULT NULL COMMENT '错误信息',
    cost_time   BIGINT       DEFAULT NULL COMMENT '耗时(ms)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_log_user (user_id),
    INDEX idx_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -----------------------------------------------------------
-- 7. 客户表
-- -----------------------------------------------------------
CREATE TABLE t_customer (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    customer_no     VARCHAR(50)  NOT NULL COMMENT '客户编号（系统生成）',
    customer_name   VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person  VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    phone           VARCHAR(20)  NOT NULL COMMENT '联系电话',
    address         VARCHAR(200) DEFAULT NULL COMMENT '地址',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    remark          TEXT         DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT       DEFAULT NULL COMMENT '更新人',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    UNIQUE KEY uk_customer_no (customer_no),
    INDEX idx_customer_name (customer_name),
    INDEX idx_customer_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- -----------------------------------------------------------
-- 8. 刀模表
-- -----------------------------------------------------------
CREATE TABLE t_knife_mold (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    mold_no         VARCHAR(50)  NOT NULL COMMENT '刀模编号（系统生成）',
    mold_name       VARCHAR(100) NOT NULL COMMENT '刀模名称',
    shape_type      VARCHAR(20)  NOT NULL COMMENT '形状：RECTANGLE/CIRCLE/OVAL/CUSTOM',
    length          DECIMAL(10,2) DEFAULT NULL COMMENT '长度mm',
    width           DECIMAL(10,2) DEFAULT NULL COMMENT '宽度mm',
    diameter        DECIMAL(10,2) DEFAULT NULL COMMENT '直径mm',
    model           VARCHAR(50)  NOT NULL COMMENT '型号（自动生成）',
    area_code       VARCHAR(10)  NOT NULL COMMENT '区域：A/B/C',
    shelf_no        VARCHAR(10)  NOT NULL COMMENT '货架号',
    layer_no        VARCHAR(10)  NOT NULL COMMENT '层号',
    position_no     VARCHAR(10)  NOT NULL COMMENT '位置号',
    location_code   VARCHAR(50)  NOT NULL COMMENT '完整位置编码：A-05-02-01',
    status          VARCHAR(20)  NOT NULL DEFAULT 'IN_STOCK' COMMENT 'IN_STOCK/OUT_STOCK',
    remark          TEXT         DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT       DEFAULT NULL COMMENT '更新人',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    UNIQUE KEY uk_mold_no (mold_no),
    INDEX idx_mold_location (location_code),
    INDEX idx_mold_status (status),
    INDEX idx_mold_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刀模表';

-- -----------------------------------------------------------
-- 9. 订单表
-- -----------------------------------------------------------
CREATE TABLE t_order (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_no        VARCHAR(50)  NOT NULL COMMENT '订单号（系统生成）',
    order_date      DATETIME     NOT NULL COMMENT '下单日期',
    delivery_no     VARCHAR(50)  NOT NULL COMMENT '送货单号',
    print_name      VARCHAR(200) NOT NULL COMMENT '印刷名称',
    quantity        INT          NOT NULL COMMENT '数量',
    unit_price      DECIMAL(10,2) NOT NULL COMMENT '单价',
    amount          DECIMAL(10,2) NOT NULL COMMENT '金额（自动计算）',
    schedule_no     VARCHAR(50)  DEFAULT NULL COMMENT '排单号',
    material        VARCHAR(100) DEFAULT NULL COMMENT '不干胶材料',
    customer_id     BIGINT       NOT NULL COMMENT '客户ID',
    customer_name   VARCHAR(100) NOT NULL COMMENT '客户名称（冗余）',
    mold_id         BIGINT       DEFAULT NULL COMMENT '刀模ID',
    mold_name       VARCHAR(100) DEFAULT NULL COMMENT '刀模名称（冗余）',
    remark          TEXT         DEFAULT NULL COMMENT '备注',
    shipped         TINYINT      NOT NULL DEFAULT 0 COMMENT '0未出货 1已出货',
    delivery_date   DATETIME     DEFAULT NULL COMMENT '送货日期',
    extra_info      VARCHAR(200) DEFAULT NULL COMMENT '扩展字段',
    reserve_field   VARCHAR(200) DEFAULT NULL COMMENT '冗余字段',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT       DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT       DEFAULT NULL COMMENT '更新人',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_date (order_date),
    INDEX idx_order_shipped (shipped),
    INDEX idx_order_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
