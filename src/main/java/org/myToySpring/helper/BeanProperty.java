package org.myToySpring.helper;

import lombok.Data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 这玩意用来定义一个受管理的Bean的一些属性
 */
@Data
public class BeanProperty {

    private String beanId;                               //bean的名字
    private Class<?> beanType;                           //这个bean的type
    private Object bean;                                 //我就是Bean本Bean了
    private Set<String> necessaryDependencies;           //构造器依赖的beanId
    private Set<String> fieldDependencies;               //域构造器以来的beanId;
    private boolean fieldDependenciesMeet = false;       //域构造器依赖已经满足
    private Method method;                               //或者方法
    private Constructor constructor;                     //构造函数
}
