package com.cartservice.anotation;

import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Logger {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    @Around("@within(logExecution) @annotation(logExecution)")
    public Object logMethod(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();  // Params name
        StringBuilder params = new StringBuilder();
//        joining params name with params value
        for (int i = 0; i < parameterNames.length; i++) {
            params.append(parameterNames[i]).append("=").append(args[i]).append(", ");
        }
        // adding params to log
        LOGGER.info("[{}#{}] called with params: {}", className, methodName, params.toString());

        Object result = null;
        try {
            // execute procedure
            result = joinPoint.proceed();
            // adding result to log
            if (signature.getReturnType().equals(Void.TYPE)) {
                LOGGER.debug("[{}#{}] returned: void", className, methodName);
            } else {
                LOGGER.debug("[{}#{}] returned: {}", className, methodName, result);
            }

//            LOGGER.info("{} - returned: {}", methodName, result);
        } catch (Exception ex) {
            // if exception occurs we add it to log
            LOGGER.error("[{}#{}] encountered exception: {}", className, methodName, ex.getMessage(), ex);
            throw ex;
        }

        return result;
    }

}
