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
    public void processMethod() {
    }

    @Pointcut("@within(com.gysoft.jdbc.multi.BindPoint)")
    public void processClass() {
    }

    @Around("processMethod()||processClass()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object object = point.getTarget();
        BindPoint classBindPoint = object.getClass().getAnnotation(BindPoint.class);
        if (classBindPoint != null) {
            DataSourceBindHolder.setDataSource(DataSourceBind.bindPoint(classBindPoint));
        }
        String methodName = point.getSignature().getName();
        MethodSignature methodSignature = ((MethodSignature) point.getSignature());
        Class<?>[] parameterTypes = methodSignature.getMethod().getParameterTypes();
        Method method = object.getClass().getMethod(methodName, parameterTypes);
        BindPoint methodBindPoint = method.getAnnotation(BindPoint.class);
        if (methodBindPoint != null) {
            DataSourceBindHolder.setDataSource(DataSourceBind.bindPoint(methodBindPoint));
        }
        try {
            return point.proceed();
        } finally {
            DataSourceBindHolder.clearDataSource();
        }
    }
}
