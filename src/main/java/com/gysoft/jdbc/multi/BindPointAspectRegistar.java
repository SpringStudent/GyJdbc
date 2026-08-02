package com.gysoft.jdbc.multi;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author 周宁
 */
public class BindPointAspectRegistar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String beanName = BindPointAspect.class.getName();
        if (!registry.containsBeanDefinition(beanName)) {
            RootBeanDefinition root = new RootBeanDefinition();
            root.setBeanClass(BindPointAspect.class);
            registry.registerBeanDefinition(beanName, root);
        }
    }

}
