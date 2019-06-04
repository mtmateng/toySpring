package org.myToySpring.runner;

import org.myToySpring.annotations.ToyComponentScan;
import org.myToySpring.context.ToySpringContext;

import java.lang.annotation.Annotation;

public class ToySpringRunner {

    public static ToySpringContext run(Class<?> mainClass, String[] args) {

        ToySpringContext context = new ToySpringContext();

        Annotation[] annotations = mainClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == ToyComponentScan.class) {
                buildContext(mainClass, context);
            }
        }
        return context;

    }

    private static void buildContext(Class<?> mainClass, ToySpringContext context) {

        ToyInnerContext toyInnerContext = new ToyInnerContext(mainClass);
        for (String beanId : toyInnerContext.getBeanId2BeanProperty().keySet()) {
            context.registerBean(beanId, toyInnerContext.getBeanId2BeanProperty().get(beanId).getBean());
            context.registerBean(toyInnerContext.getBeanId2BeanProperty().get(beanId).getBeanType(),
                    toyInnerContext.getBeanId2BeanProperty().get(beanId).getBean(),
                    toyInnerContext.getBeanId2BeanProperty().get(beanId).isPrimary());
        }

    }

}