package org.myToySpring.helper;

import lombok.Data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 这玩意用来定义一个受管理的Bean的一些属性
 */
@Data
public class BeanProperty {

    private String beanId;                                  // bean的名字
    private Class<?> beanType;                              // 这个bean的type
    private BeanGenerateType beanGenerateType;              // bean生成的方式，@Component或者@Bean
    private Object bean;                                    // 我就是Bean本Bean了
    /**
     * 这里要重点讲一下这两个概念，necessaryDependencies是构造器需要的，即没有就没法成功实例化类的依赖
     * 而fieldDependencies是被@Autowired标注的field的依赖。并不是说他们不是必要的，实际上，当
     * fieldDependencies依赖的bean无法找到的时，默认情况也会产生异常。这和Spring的表现是一致的。
     * Spring的@Autowired有是否允许null的选项，我们的这个玩具项目也会提供。
     */
    private List<String> necessaryDependencies;             // 构造器依赖的beanId
    private List<String> fieldDependencies;                 // 域构造器依赖的beanId
    private boolean fieldDependenciesMeet = false;          // 域构造器依赖已经满足
    private Constructor constructor;                        // 构造函数
    private Method method;                                  // 或者生成该Bean的方法
    private String methodRestedComponentId;                 // bean生成的那个实体的ID

    public enum BeanGenerateType {
        Component,
        MethodBean,
    }

}
