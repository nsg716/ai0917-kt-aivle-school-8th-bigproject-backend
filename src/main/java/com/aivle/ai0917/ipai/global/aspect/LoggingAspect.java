package com.aivle.ai0917.ipai.global.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // Controller 메서드 실행 전
    @Before("execution(* com.aivle.ai0917.ipai.controller.*.*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        log.info("==> [Controller] {}.{} 호출 - 파라미터: {}",
                className, methodName, Arrays.toString(args));
    }

    // Controller 메서드 실행 후 (정상 반환)
    @AfterReturning(
            pointcut = "execution(* com.aivle.ai0917.ipai.controller.*.*(..))",
            returning = "result"
    )
    public void logAfterReturningController(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("<== [Controller] {}.{} 정상 완료 - 반환값: {}",
                className, methodName, result);
    }

    // Service 메서드 실행 시간 측정
    @Around("execution(* com.aivle.ai0917.ipai.service.*.*(..))")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        log.info(">>> [Service] {}.{} 시작 - 파라미터: {}",
                className, methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("<<< [Service] {}.{} 완료 - 실행시간: {}ms",
                    className, methodName, executionTime);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("XXX [Service] {}.{} 예외 발생 - 실행시간: {}ms, 예외: {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    // 예외 발생 시
    @AfterThrowing(
            pointcut =
                    "execution(* com.aivle.ai0917.ipai.controller.*.*(..)) || " +
                            "execution(* com.aivle.ai0917.ipai.service.*.*(..))",
            throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.error("!!! [Exception] {}.{} - 예외 타입: {}, 메시지: {}",
                className, methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());
    }

    // Repository 메서드 로깅 (선택사항)
    @Before("execution(* com.aivle.ai0917.ipai.repository.*.*(..))")
    public void logBeforeRepository(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("[Repository] {}.{} 호출", className, methodName);
    }
}