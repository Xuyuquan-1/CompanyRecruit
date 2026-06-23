package com.hrrecruit.common.aspect;

import com.hrrecruit.common.annotation.LogOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("@annotation(com.hrrecruit.common.annotation.LogOperation)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        LogOperation logOperation = method.getAnnotation(LogOperation.class);

        long startTime = System.currentTimeMillis();

        // 记录请求信息
        if (log.isInfoEnabled()) {
            log.info("[操作日志] 模块: {}, 类型: {}, 描述: {}",
                    logOperation.module(),
                    logOperation.type(),
                    logOperation.desc());
        }

        Object result;
        try {
            result = point.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[操作日志] 执行成功, 耗时: {}ms", costTime);
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[操作日志] 执行失败, 耗时: {}ms, 异常: {}", costTime, e.getMessage());
            throw e;
        }

        return result;
    }
}