package org.myToySpring.utils;

import org.myToySpring.annotations.*;
import org.myToySpring.exceptions.ContextInitException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.Locale.ENGLISH;

public class BeanNameUtils {

    public static String getComponentId(Class aClass) {

        List<String> names = new ArrayList<>();
        for (Annotation annotation : aClass.getDeclaredAnnotations()) {
            if (annotation instanceof ToyService && !((ToyService) annotation).value().equals("")) {
                names.add(((ToyService) annotation).value());
            } else if (annotation instanceof ToyRepository && !((ToyRepository) annotation).value().equals("")) {
                names.add(((ToyRepository) annotation).value());
            } else if (annotation instanceof ToyComponent && !((ToyComponent) annotation).value().equals("")) {
                names.add(((ToyComponent) annotation).value());
            }
        }
        if (names.size() > 1) {
            throw new ContextInitException(String.format("class %s 上发现了多个注解赋予其不同的name，请检查", aClass.getName()));
        } else if (names.size() == 1) {
            return names.get(0);
        } else {
            return lowerCaseFirstChar(aClass.getSimpleName());
        }
    }

    public static String getBeanId(Method beanMethod) {
        ToyBean toyBean = beanMethod.getAnnotation(ToyBean.class);
        String name = toyBean.name();
        if ("".equals(name)) {
            name = lowerCaseFirstChar(beanMethod.getName());
        }
        return name;
    }

    private static String lowerCaseFirstChar(String name) {
        if (name == null || name.equals("")) {
            return name;
        }
        return name.substring(0, 1).toLowerCase(ENGLISH) + name.substring(1);
    }

}
