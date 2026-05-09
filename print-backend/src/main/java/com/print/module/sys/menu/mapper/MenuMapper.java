package com.print.module.sys.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.print.module.sys.menu.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuMapper extends BaseMapper<SysMenu> {
}
