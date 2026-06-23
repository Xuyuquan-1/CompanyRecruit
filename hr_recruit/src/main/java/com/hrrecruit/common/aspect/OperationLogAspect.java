package com.hrrecruit.common.aspect;

import cn.hutool.json.JSONUtil;
import com.hrrecruit.common.annotation.OperationLog;
import com.hrrecruit.entity.SysAuditLog;
import com.hrrecruit.mapper.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysAuditLogMapper sysAuditLogMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        SysAuditLog sysLog = new SysAuditLog();
        sysLog.setOperation(operationLog.module() + "-" + operationLog.type() + ": " + operationLog.description());
        sysLog.setCreateTime(LocalDateTime.now());

        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysLog.setMethod(request.getMethod() + " " + request.getRequestURL().toString());
                sysLog.setIpAddress(getIpAddress(request));
                sysLog.setUserAgent(request.getHeader("User-Agent"));

                // 获取请求参数
                Object[] args = joinPoint.getArgs();
                if (args.length > 0) {
                    try {
                        String params = JSONUtil.toJsonStr(args);
                        if (params.length() > 2000) {
                            params = params.substring(0, 2000) + "...";
                        }
                        sysLog.setRequestParams(params);
                    } catch (Exception e) {
                        log.warn("序列化请求参数失败: {}", e.getMessage());
                    }
                }
            }

            // 获取用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                try {
                    sysLog.setUserId(Long.parseLong(authentication.getName()));
                    sysLog.setUsername(authentication.getName());
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录成功日志
            sysLog.setStatus(1);
            try {
                String responseData = JSONUtil.toJsonStr(result);
                if (responseData.length() > 2000) {
                    responseData = responseData.substring(0, 2000) + "...";
                }
                sysLog.setResponseResult(responseData);
            } catch (Exception e) {
                log.warn("序列化响应数据失败: {}", e.getMessage());
            }

            sysLog.setExecuteTime(System.currentTimeMillis() - startTime);

            // 异步保存日志
            saveLogAsync(sysLog);

            return result;

        } catch (Throwable e) {
            // 记录失败日志
            sysLog.setStatus(0);
            sysLog.setErrorMsg(e.getMessage());
            sysLog.setExecuteTime(System.currentTimeMillis() - startTime);

            // 异步保存日志
            saveLogAsync(sysLog);

            throw e;
        }
    }

    @Async
    public void saveLogAsync(SysAuditLog sysLog) {
        sysAuditLogMapper.insert(sysLog);
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}