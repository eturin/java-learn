package ture.app.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ture.app.native_sql.AppLocks;

@Aspect
@Component
public class LockAspect {
    @Autowired
    private AppLocks appLocks;

    @Before("execution(* ture.app.repository.*.save(..))")
    public void lock(JoinPoint joinPoint) {
        Object entity = joinPoint.getArgs()[0];
        appLocks.lock(entity);
    }
}
