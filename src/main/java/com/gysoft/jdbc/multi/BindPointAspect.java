package com.gysoft.jdbc.multi;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * @author 周宁
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BindPointAspect {

    @Pointcut("@annotation(com.gysoft.jdbc.multi.BindPoint)")
    public void processMethod() {
    }

    @Pointcut("@within(com.gysoft.jdbc.multi.BindPoint)")
    public void processClass() {
    }

    @Around("processMethod()||processClass()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        BindPoint bindPoint = resolveBindPoint(point);
        if (bindPoint == null) {
            return point.proceed();
        }
        DataSourceBindHolder.pushDataSource(DataSourceBind.bindPoint(bindPoint));
        try {
            return point.proceed();
        } finally {
            DataSourceBindHolder.popDataSource();
        }
    }

    private BindPoint resolveBindPoint(ProceedingJoinPoint point) {
        Class<?> targetClass = ClassUtils.getUserClass(point.getTarget());
        Method signatureMethod = ((MethodSignature) point.getSignature()).getMethod();
        Method targetMethod = AopUtils.getMostSpecificMethod(signatureMethod, targetClass);
        BindPoint methodBindPoint = AnnotatedElementUtils.findMergedAnnotation(targetMethod, BindPoint.class);
        if (methodBindPoint != null) {
            return methodBindPoint;
        }
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, BindPoint.class);
    }
}
