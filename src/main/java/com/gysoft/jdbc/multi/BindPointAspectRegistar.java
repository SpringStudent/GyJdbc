package com.gysoft.jdbc.multi;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author 周宁
 */
public class BindPointAspectRegistar implements ImportBeanDefinitionRegistrar {
    /**
     * 是否已经初始化
     */
    private static boolean inited;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!inited) {
            RootBeanDefinition root = new RootBeanDefinition();
            root.setBeanClass(BindPointAspect.class);
            registry.registerBeanDefinition(BindPointAspect.class.getName(), root);
            inited = true;
        }
    }

}
