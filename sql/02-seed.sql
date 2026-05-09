-- ============================================================
-- 印刷行业综合管理系统 - 初始数据脚本
-- ============================================================

USE print_management;

-- -----------------------------------------------------------
-- 1. 预设角色（密码为 BCrypt 加密的 "admin123"）
--    BCrypt 密码: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
-- -----------------------------------------------------------
INSERT INTO sys_role (role_name, role_code, description, status, create_by) VALUES
('超级管理员', 'super_admin', '系统管理员，拥有全部权限', 1, 1),
('财务人员', 'finance', '可查看订单及金额，操作统计报表', 1, 1),
('普通员工', 'employee', '负责日常录单、刀模位置打印、客户信息维护', 1, 1);

-- -----------------------------------------------------------
-- 2. 默认管理员账号 (admin / admin123)
--    密码使用 bcryptjs 生成 ($2b$ 兼容 Spring Security)
-- -----------------------------------------------------------
INSERT INTO sys_user (username, password, real_name, status, create_by) VALUES
('admin', '$2b$10$qnVfhr4f4MdQ2tdxdoEVYOAo3tHWwc9mfDGUYVYg6NLKWpC1u3Yj6', '系统管理员', 1, 1);

-- -----------------------------------------------------------
-- 3. 赋予 admin 超级管理员角色
-- -----------------------------------------------------------
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- -----------------------------------------------------------
-- 4. 菜单与权限数据
-- menu_type: 0=目录, 1=菜单, 2=按钮
-- -----------------------------------------------------------
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, sort, menu_type, permission) VALUES
-- 一级目录
(1, NULL, '仪表盘', '/dashboard', 'dashboard/DashboardView', 'Odometer', 1, 1, 'dashboard:view'),
(2, NULL, '订单管理', '/order', 'order/OrderList', 'List', 2, 1, 'order:list'),
(3, NULL, '客户管理', '/customer', 'customer/CustomerList', 'User', 3, 1, 'customer:list'),
(4, NULL, '刀模管理', '/knife-mold', 'mold/KnifeMoldList', 'Tools', 4, 1, 'mold:list'),
(5, NULL, '数据统计', '/statistics', 'statistics/StatisticsView', 'DataAnalysis', 5, 1, 'stats:view'),
(6, NULL, '系统管理', NULL, NULL, 'Setting', 99, 0, NULL),

-- 系统管理子菜单
(7, 6, '用户管理', '/system/user', 'system/user/UserList', NULL, 1, 1, 'sys:user:list'),
(8, 6, '角色管理', '/system/role', 'system/role/RoleList', NULL, 2, 1, 'sys:role:list'),
(9, 6, '菜单管理', '/system/menu', 'system/menu/MenuList', NULL, 3, 1, 'sys:menu:list'),
(10, 6, '操作日志', '/system/log', 'system/log/LogList', NULL, 4, 1, 'sys:log:list'),

-- 订单管理按钮权限
(11, 2, '新增订单', NULL, NULL, NULL, 1, 2, 'order:create'),
(12, 2, '编辑订单', NULL, NULL, NULL, 2, 2, 'order:update'),
(13, 2, '删除订单', NULL, NULL, NULL, 3, 2, 'order:delete'),
(14, 2, '导入订单', NULL, NULL, NULL, 4, 2, 'order:import'),
(15, 2, '导出订单', NULL, NULL, NULL, 5, 2, 'order:export'),
(16, 2, '打印送货单', NULL, NULL, NULL, 6, 2, 'order:print'),

-- 客户管理按钮权限
(17, 3, '新增客户', NULL, NULL, NULL, 1, 2, 'customer:create'),
(18, 3, '编辑客户', NULL, NULL, NULL, 2, 2, 'customer:update'),
(19, 3, '删除客户', NULL, NULL, NULL, 3, 2, 'customer:delete'),
(20, 3, '导入客户', NULL, NULL, NULL, 4, 2, 'customer:import'),
(21, 3, '导出客户', NULL, NULL, NULL, 5, 2, 'customer:export'),

-- 刀模管理按钮权限
(22, 4, '新增刀模', NULL, NULL, NULL, 1, 2, 'mold:create'),
(23, 4, '编辑刀模', NULL, NULL, NULL, 2, 2, 'mold:update'),
(24, 4, '删除刀模', NULL, NULL, NULL, 3, 2, 'mold:delete'),
(25, 4, '打印标签', NULL, NULL, NULL, 4, 2, 'mold:print'),

-- 系统管理按钮权限
(26, 7, '新增用户', NULL, NULL, NULL, 1, 2, 'sys:user:create'),
(27, 7, '编辑用户', NULL, NULL, NULL, 2, 2, 'sys:user:update'),
(28, 7, '删除用户', NULL, NULL, NULL, 3, 2, 'sys:user:delete'),
(29, 8, '新增角色', NULL, NULL, NULL, 1, 2, 'sys:role:create'),
(30, 8, '编辑角色', NULL, NULL, NULL, 2, 2, 'sys:role:update'),
(31, 8, '删除角色', NULL, NULL, NULL, 3, 2, 'sys:role:delete'),
(32, 8, '分配权限', NULL, NULL, NULL, 4, 2, 'sys:role:assign'),
(33, 9, '新增菜单', NULL, NULL, NULL, 1, 2, 'sys:menu:create'),
(34, 9, '编辑菜单', NULL, NULL, NULL, 2, 2, 'sys:menu:update'),
(35, 9, '删除菜单', NULL, NULL, NULL, 3, 2, 'sys:menu:delete'),
(36, 10, '导出日志', NULL, NULL, NULL, 1, 2, 'sys:log:export');

-- -----------------------------------------------------------
-- 5. 角色-菜单权限分配
-- -----------------------------------------------------------
-- 超级管理员: 所有菜单和权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE is_deleted = 0;

-- 财务人员: 订单查看 + 统计 + 操作日志
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE id IN (1, 2, 5, 10, 14, 15, 36);

-- 普通员工: 订单(不含打印/导入/导出) + 客户 + 刀模
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE id IN (1, 2, 3, 4, 11, 12, 13, 17, 18, 19, 22, 23, 24, 25);
