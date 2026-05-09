package com.print.module.sys.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.print.common.exception.BusinessException;
import com.print.common.util.SecurityUtil;
import com.print.module.sys.user.entity.SysUser;
import com.print.module.sys.user.entity.SysUserRole;
import com.print.module.sys.user.mapper.UserMapper;
import com.print.module.sys.user.mapper.UserRoleMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, UserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public IPage<SysUser> getPage(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Transactional
    public void create(SysUser user) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            user.setCreateBy(currentUserId);
            user.setUpdateBy(currentUserId);
        }
        userMapper.insert(user);
    }

    @Transactional
    public void update(SysUser user) {
        SysUser existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getUsername() != null && !user.getUsername().equals(existing.getUsername())) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            user.setUpdateBy(currentUserId);
        }
        // Prevent clearing password via update
        user.setPassword(null);
        userMapper.updateById(user);
    }

    @Transactional
    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    @Transactional
    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }
}
