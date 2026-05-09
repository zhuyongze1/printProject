package com.print.module.sys.menu;

import com.print.common.result.Result;
import com.print.module.sys.menu.entity.SysMenu;
import com.print.module.sys.menu.mapper.MenuMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;
    private final MenuMapper menuMapper;

    public MenuController(MenuService menuService, MenuMapper menuMapper) {
        this.menuService = menuService;
        this.menuMapper = menuMapper;
    }

    @GetMapping
    public Result<List<SysMenu>> getTree() {
        return Result.success(menuService.getMenuTree());
    }

    @PostMapping
    @Transactional
    public Result<Void> create(@RequestBody SysMenu menu) {
        menuMapper.insert(menu);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        menuMapper.updateById(menu);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        menuMapper.deleteById(id);
        return Result.success();
    }
}
