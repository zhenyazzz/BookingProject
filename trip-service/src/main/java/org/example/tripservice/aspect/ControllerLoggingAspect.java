package org.example.tripservice.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerCall(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        String method = pjp.getSignature().toShortString();
        String user = resolveUser();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("CALL {} user={} duration={}ms",
                    method, user, duration);

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;

            log.error("ERROR {} user={} duration={}ms message={}",
                    method, user, duration, ex.getMessage(), ex);

            throw ex;
        }
    }

    private String resolveUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }
        return auth.getName();
    }
}

