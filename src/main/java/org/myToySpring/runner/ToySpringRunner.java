package org.myToySpring.runner;

import org.myToySpring.constants.ComponentAnnotations;
import org.myToySpring.constants.ComponentFullName;
import org.myToySpring.context.ToySpringContext;
import org.myToySpring.exceptions.ContextInitException;
import org.myToySpring.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.*;

public class ToySpringRunner {

    public static ToySpringContext run(Class<?> mainClass, String[] args) {

        ToySpringContext context = new ToySpringContext();

        Annotation[] annotations = mainClass.getAnnotations();
        for (Annotation annotation : annotations) {
            switch (annotation.getClass().getName()) {
                case ComponentFullName.TOY_COMPONENT_SCAN:
                    buildContext(mainClass, context);
                    break;
                default:
                    break;
            }
        }
        return context;

    }

    private static void buildContext(Class<?> mainClass, ToySpringContext context) {

        // 得到扫描包下的所有类，并注册到上下文中
        List<Class<?>> classes = registerBean(mainClass, context);

        // 接下来需要处理依赖关系。因为如果A依赖B，B依赖A，那么A和B都无法成功创建。Spring的默认策略比较有意思，当使用构造函数进行Autowired
        // 造成依赖时，将会出错。但假如是在域上或者在set方法上使用Autowired，造成相互依赖，将不会出错。只需构建完两个方法，再将值set进去就行
        // 这种策略相当合理，也更加灵活。我们在这里模仿这个策略。
        List<String> sortedClass = buildDAG(classes, context);

        //接下来正式开始初始化
        instanceBean(sortedClass, context);


    }

    /**
     * 开始正式初始化bean，因为已经构造成一个有序列表，我们逐一初始化就好
     * @param sortedClass
     */
    private static void instanceBean(List<String> sortedClass, ToySpringContext context) {
        for (String classId : sortedClass) {
            Class aClass = context.getTypeById(classId);


        }
    }

}