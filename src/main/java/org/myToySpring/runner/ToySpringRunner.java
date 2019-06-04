package org.myToySpring.runner;

import org.myToySpring.annotations.ToyComponentScan;
import org.myToySpring.context.ToySpringBeanContext;

import java.lang.annotation.Annotation;

public class ToySpringRunner {

    public static ToySpringBeanContext run(Class<?> mainClass, String[] args) {

        ToySpringBeanContext context = new ToySpringBeanContext();

        Annotation[] annotations = mainClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == ToyComponentScan.class) {
                buildContext(mainClass, context);
            }
        }
        return context;

    }

    private static void buildContext(Class<?> mainClass, ToySpringBeanContext context) {

        ToyInnerContext toyInnerContext = new ToyInnerContext(mainClass);
        for (String beanId : toyInnerContext.getBeanId2BeanProperty().keySet()) {
            context.registerBean(beanId, toyInnerContext.getBeanId2BeanProperty().get(beanId).getBean());
            context.registerBean(toyInnerContext.getBeanId2BeanProperty().get(beanId).getBeanType(),
                    toyInnerContext.getBeanId2BeanProperty().get(beanId).getBean(),
                    toyInnerContext.getBeanId2BeanProperty().get(beanId).isPrimary());
        }

    }

}