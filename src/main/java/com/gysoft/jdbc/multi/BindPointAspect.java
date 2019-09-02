package com.gysoft.jdbc.multi;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author 周宁
 */
@Aspect
public class BindPointAspect {

    @Pointcut("@annotation(com.gysoft.jdbc.multi.BindPoint)")
    public void dataSourceChange() {
    }

    @Around("dataSourceChange()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object object = point.getTarget();
        String methodName = point.getSignature().getName();
        MethodSignature methodSignature = ((MethodSignature)point.getSignature());
        Class<?>[] parameterTypes = methodSignature.getMethod().getParameterTypes();
        Method method = object.getClass().getMethod(methodName, parameterTypes);
        BindPoint bindPoint = method.getAnnotation(BindPoint.class);
        if (bindPoint != null) {
            DataSourceIdHolder.setDataSource(bindPoint.value(), BindPointType.ByAnno);
        }
        try {
            return point.proceed();
        } finally {
            DataSourceIdHolder.clearDataSource();
        }
    }
}
