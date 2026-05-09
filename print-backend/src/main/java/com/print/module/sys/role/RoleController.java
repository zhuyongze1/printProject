package com.print.module.sys.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.print.common.exception.BusinessException;
import com.print.common.result.Result;
import com.print.module.sys.role.entity.SysRole;
import com.print.module.sys.role.entity.SysRoleMenu;
import com.print.module.sys.role.mapper.RoleMapper;
import com.print.module.sys.role.mapper.RoleMenuMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    public RoleController(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @GetMapping
    public Result<List<SysRole>> list() {
        return Result.success(roleMapper.selectList(null));
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return Result.success(role);
    }

    @PostMapping
    @Transactional
    public Result<Void> create(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}/menus")
    public Result<List<Long>> getMenus(@PathVariable Long id) {
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        return Result.success(menuIds);
    }

    @PutMapping("/{id}/menus")
    @Transactional
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        List<Long> menuIds = body.get("menuIds");
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        return Result.success();
    }
}
