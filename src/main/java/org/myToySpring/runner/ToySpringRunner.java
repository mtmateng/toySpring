package org.myToySpring.runner;

import org.myToySpring.annotations.ToyComponentScan;
import org.myToySpring.context.ToySpringBeanContext;
import org.myToySpring.context.ToySpringConfigurationContext;

import java.lang.annotation.Annotation;

public class ToySpringRunner {

    public static ToySpringBeanContext run(Class<?> mainClass, String[] args) {

        ToySpringBeanContext beanContext = new ToySpringBeanContext();
        ToySpringConfigurationContext configurationContext = new ToySpringConfigurationContext(args);

        Annotation[] annotations = mainClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == ToyComponentScan.class) {
                buildContext(mainClass, beanContext, configurationContext);
            }
        }
        return beanContext;

    }

    private static void buildContext(Class<?> mainClass, ToySpringBeanContext context, ToySpringConfigurationContext configurationContext) {

        ToyInnerContext toyInnerContext = new ToyInnerContext(mainClass,configurationContext);
        for (String beanId : toyInnerContext.getBeanId2BeanProperty().keySet()) {
            context.registerBean(beanId, toyInnerContext.getBeanId2BeanProperty().get(beanId).getBean());
            context.registerBean(toyInnerContext.getBeanId2BeanProperty().get(beanId).getBeanType(),
                    toyInnerContext.getBeanId2BeanProperty().get(beanId).getBean(),
                    toyInnerContext.getBeanId2BeanProperty().get(beanId).isPrimary());
        }

    }

}