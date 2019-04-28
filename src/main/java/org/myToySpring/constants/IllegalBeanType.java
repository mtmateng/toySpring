package org.myToySpring.constants;

import org.myToySpring.exceptions.ContextInitException;

import java.util.*;

public class IllegalBeanType {

    private final static Set<Class> illegalBeanTypes = new HashSet<>(Arrays.asList(
        void.class, String.class,
        char.class, Character.class,
        byte.class, Byte.class,
        int.class, Integer.class,
        boolean.class, Boolean.class,
        short.class, Short.class,
        long.class, Long.class,
        double.class, Double.class,
        float.class, Float.class,
        Collection.class, Map.class));

    /**
     * 各种基本类型和String类型都不接受作为合法的Bean类型
     * 而且不接受集合类，不接受数组类。
     */
    public static void checkBeanTypeLegallity(Class aClass) {
        if (illegalBeanTypes.contains(aClass) || aClass.isArray()) {
            throw new ContextInitException("尝试将一个基本类型或集合类型注册为Bean");
        }
    }

}
