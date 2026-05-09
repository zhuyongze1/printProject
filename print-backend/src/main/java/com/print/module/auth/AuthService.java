package com.print.module.auth;

import com.print.common.exception.BusinessException;
import com.print.common.util.JwtUtil;
import com.print.common.util.SecurityUtil;
import com.print.module.auth.dto.LoginRequest;
import com.print.module.auth.dto.RegisterRequest;
import com.print.module.sys.menu.entity.SysMenu;
import com.print.module.sys.menu.mapper.MenuMapper;
import com.print.module.sys.role.entity.SysRole;
import com.print.module.sys.role.mapper.RoleMapper;
import com.print.module.sys.role.mapper.RoleMenuMapper;
import com.print.module.sys.role.entity.SysRoleMenu;
import com.print.module.sys.user.entity.SysUser;
import com.print.module.sys.user.entity.SysUserRole;
import com.print.module.sys.user.mapper.UserMapper;
import com.print.module.sys.user.mapper.UserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper,
                       MenuMapper menuMapper, UserRoleMapper userRoleMapper,
                       RoleMenuMapper roleMenuMapper,
                       JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> login(LoginRequest req) {
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        List<String> permissions = getUserPermissions(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), permissions);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return result;
    }

    @Transactional
    public void register(RegisterRequest req) {
        Long count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getRealName());
        user.setStatus(1);
        userMapper.insert(user);

        // Assign default role "employee" (role_code = 'employee')
        SysRole employeeRole = roleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, "employee"));
        if (employeeRole != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(employeeRole.getId());
            userRoleMapper.insert(ur);
        }
    }

    public Map<String, Object> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        List<String> permissions = getUserPermissions(userId);
        List<SysRole> roles = getUserRoles(userId);

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("realName", user.getRealName());
        userMap.put("phone", user.getPhone());
        userMap.put("email", user.getEmail());

        result.put("user", userMap);
        result.put("permissions", permissions);
        result.put("roles", roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        return result;
    }

    public List<String> getUserPermissions(Long userId) {
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRoleMenu>()
                        .inSql(SysRoleMenu::getRoleId,
                                "SELECT role_id FROM sys_user_role WHERE user_id = " + userId));

        if (roleMenus.isEmpty()) return List.of();

        Set<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());

        if (menuIds.isEmpty()) return List.of();

        List<SysMenu> menus = menuMapper.selectBatchIds(menuIds);
        return menus.stream()
                .filter(m -> m.getPermission() != null && !m.getPermission().isEmpty())
                .map(SysMenu::getPermission)
                .collect(Collectors.toList());
    }

    private List<SysRole> getUserRoles(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) return List.of();

        Set<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());
        return roleMapper.selectBatchIds(roleIds);
    }
}
