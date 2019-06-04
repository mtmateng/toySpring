package org.myToySpring.context;

import org.myToySpring.exceptions.ContextInitException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToySpringBeanContext {

    private final Map<String, Object> name2BeanMap = new HashMap<>();
    private final Map<Class, List<Object>> beanType2Bean = new HashMap<>();

    public Object getBean(String name) {
        return name2BeanMap.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        if (beanType2Bean.get(type) != null && !beanType2Bean.get(type).isEmpty()) {
            return (T) beanType2Bean.get(type).get(0);
        } else {
            throw new ContextInitException(String.format("没有找到类型为%s的Bean", type.getName()));
        }
    }

    public void registerBean(String name, Object bean) {

        name2BeanMap.put(name, bean);

    }

    // 如果是Primary，就插入第一个，否则就插入到后面
    public void registerBean(Class aClass, Object bean, boolean isPrimary) {

        beanType2Bean.putIfAbsent(aClass, new ArrayList<>());
        if (isPrimary) {
            beanType2Bean.get(aClass).add(0, bean);
        } else {
            beanType2Bean.get(aClass).add(bean);
        }

    }

}
