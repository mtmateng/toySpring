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
    /**
     * 这里要重点讲一下这两个概念，necessaryDependencies是构造器需要的，即没有就没法成功实例化类的依赖
     * 而fieldDependencies是被@Autowired标注的field的依赖。并不是说他们不是必要的，实际上，当
     * fieldDependencies依赖的bean无法找到的时，默认情况也会产生异常。这和Spring的表现是一致的。
     * Spring的@Autowired有是否允许null的选项，我们的这个玩具项目也会提供。
     */
    private Set<String> necessaryDependencies;           //构造器依赖的beanId
    private Set<String> fieldDependencies;               //域构造器依赖的beanId
    private boolean fieldDependenciesMeet = false;       //域构造器依赖已经满足
    private Method method;                               //或者方法
    private Constructor constructor;                     //构造函数
}
