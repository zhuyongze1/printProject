package com.print.aspect;

import com.print.annotation.OperateLog;
import com.print.common.util.SecurityUtil;
import com.print.module.sys.log.entity.SysOperationLog;
import com.print.module.sys.log.mapper.LogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class OperateLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperateLogAspect.class);

    private final LogMapper logMapper;
    private final HttpServletRequest request;

    public OperateLogAspect(LogMapper logMapper, HttpServletRequest request) {
        this.logMapper = logMapper;
        this.request = request;
    }

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint point, OperateLog operateLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setUserId(SecurityUtil.getCurrentUserId());
        operationLog.setUsername(SecurityUtil.getCurrentUsername());
        operationLog.setOperation(operateLog.value());
        operationLog.setMethod(point.getSignature().toShortString());
        operationLog.setParams(Arrays.toString(point.getArgs()));
        operationLog.setIp(getClientIp());

        try {
            Object result = point.proceed();
            operationLog.setStatus(1);
            operationLog.setCostTime(System.currentTimeMillis() - startTime);
            logMapper.insert(operationLog);
            return result;
        } catch (Exception e) {
            operationLog.setStatus(0);
            operationLog.setErrorMsg(e.getMessage());
            operationLog.setCostTime(System.currentTimeMillis() - startTime);
            logMapper.insert(operationLog);
            throw e;
        }
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
