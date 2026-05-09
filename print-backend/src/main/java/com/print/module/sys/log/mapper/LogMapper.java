package com.print.module.sys.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.print.module.sys.log.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<SysOperationLog> {
}
