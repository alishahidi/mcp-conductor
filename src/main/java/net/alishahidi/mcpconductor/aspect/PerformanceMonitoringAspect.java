package net.alishahidi.mcpconductor.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* net.alishahidi.mcpconductor.service.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object result = joinPoint.proceed();
            
            // Record successful execution
            sample.stop(Timer.builder("service.method.execution")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .register(meterRegistry));
            
            return result;
            
        } catch (Exception e) {
            // Record failed execution
            sample.stop(Timer.builder("service.method.execution")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry));
            
            // Increment error counter
            meterRegistry.counter("service.method.errors",
                    "class", className,
                    "method", methodName,
                    "error", e.getClass().getSimpleName())
                    .increment();
            
            throw e;
        }
    }

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object monitorToolMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object result = joinPoint.proceed();
            
            // Record successful tool execution
            sample.stop(Timer.builder("tool.execution")
                    .tag("class", className)
                    .tag("tool", methodName)
                    .tag("status", "success")
                    .register(meterRegistry));
            
            // Count tool usage
            meterRegistry.counter("tool.usage",
                    "class", className,
                    "tool", methodName)
                    .increment();
            
            return result;
            
        } catch (Exception e) {
            // Record failed tool execution
            sample.stop(Timer.builder("tool.execution")
                    .tag("class", className)
                    .tag("tool", methodName)
                    .tag("status", "error")
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry));
            
            // Count tool errors
            meterRegistry.counter("tool.errors",
                    "class", className,
                    "tool", methodName,
                    "error", e.getClass().getSimpleName())
                    .increment();
            
            throw e;
        }
    }
}