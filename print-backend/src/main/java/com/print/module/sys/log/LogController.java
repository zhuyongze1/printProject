package com.print.module.sys.log;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.print.common.result.PageResult;
import com.print.common.result.Result;
import com.print.module.sys.log.entity.SysOperationLog;
import com.print.module.sys.log.mapper.LogMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogMapper logMapper;

    public LogController(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @GetMapping
    public Result<PageResult<SysOperationLog>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysOperationLog::getUsername, keyword)
                    .or()
                    .like(SysOperationLog::getOperation, keyword));
        }
        if (startDate != null) {
            wrapper.ge(SysOperationLog::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(SysOperationLog::getCreateTime, endDate.atTime(LocalTime.MAX));
        }
        wrapper.orderByDesc(SysOperationLog::getCreateTime);
        return Result.success(PageResult.of(logMapper.selectPage(new Page<>(pageNum, pageSize), wrapper)));
    }
}
