package com.print.module.sys.menu;

import com.print.module.sys.menu.entity.SysMenu;
import com.print.module.sys.menu.mapper.MenuMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuMapper menuMapper;

    public MenuService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = menuMapper.selectList(null);
        Map<Long, List<SysMenu>> parentMap = allMenus.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() != 0)
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> tree = allMenus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .peek(m -> m.setChildren(buildChildren(m.getId(), parentMap)))
                .collect(Collectors.toList());

        return tree;
    }

    private List<SysMenu> buildChildren(Long parentId, Map<Long, List<SysMenu>> parentMap) {
        List<SysMenu> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        for (SysMenu child : children) {
            child.setChildren(buildChildren(child.getId(), parentMap));
        }
        return children;
    }
}
