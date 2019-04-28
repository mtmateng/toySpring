package org.myToySpring.context;

import org.myToySpring.constants.IllegalBeanType;
import org.myToySpring.exceptions.ContextInitException;
import org.myToySpring.helper.BeanProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToySpringContext {

    private final Map<String, BeanProperty> beanId2BeanProperty = new HashMap<>();

    private final Map<String, Object> name2BeanMap = new HashMap<>();
    private final Map<String, Class> name2BeanTypeMap = new HashMap<>();
    private final Map<Class, List<String>> beanType2Names = new HashMap<>();

    public Object getBean(String name) {
        return name2BeanMap.get(name);
    }

    public <T> T getBean(Class<T> type) {
        List<Object> objects = new ArrayList<>();
        for (Object managedBean : name2BeanMap.values()) {
            if (type.isAssignableFrom(managedBean.getClass())) {
                objects.add(managedBean);
            }
        }
        switch (objects.size()) {
            case 0:
                throw new ContextInitException(String.format("没有发现类型为%s的元素", type.getName()));
            case 1:
                return type.cast(objects.get(0));
            default:
                throw new ContextInitException(String.format("类型为%s的元素有两个，无法决定选择哪一个", type.getName()));
        }
    }

    public Class getTypeById(String name) {
        return name2BeanTypeMap.get(name);
    }

    public void registerBean(String name, Object bean) {

        if (name2BeanMap.containsKey(name)) {
            throw new ContextInitException(String.format("尝试注册name为%s，类型为%s的bean，但已有同名Bean，其类型为%s，请检查",
                name, bean.getClass().getName(), name2BeanMap.get(name).getClass().getName()));
        }
        name2BeanMap.put(name, bean);

    }

    public void registerBeanType(String name, Class aClass) {

        IllegalBeanType.checkBeanTypeLegallity(aClass);
        if (name2BeanTypeMap.containsKey(name)) {
            throw new ContextInitException(String.format("尝试注册name为%s，类型为%s的类型，但已有同名类，其类型为%s，请检查",
                name, aClass.getName(), name2BeanMap.get(name).getClass().getName()));
        }
        name2BeanTypeMap.put(name, aClass);

    }

    public void registerBeanName(Class aClass, String name) {

        IllegalBeanType.checkBeanTypeLegallity(aClass);
        beanType2Names.putIfAbsent(aClass, new ArrayList<>());
        beanType2Names.get(aClass).add(name);

    }


}
