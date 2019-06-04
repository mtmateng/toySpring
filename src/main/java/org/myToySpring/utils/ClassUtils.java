package org.myToySpring.utils;

import org.myToySpring.annotations.ToyBean;
import org.myToySpring.exceptions.ContextInitException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

@SuppressWarnings("unchecked")
public class ClassUtils {

    public static <T> List<Class<T>> getAllClassByInterface(Class<T> fatherInterface) {

        List<Class<T>> returnList = new ArrayList<>();
        if (fatherInterface.isInterface() || Modifier.isAbstract(fatherInterface.getModifiers())) {
            String packageName = fatherInterface.getPackage().getName();
            for (Class child : getAllClassInPackage(packageName)) {
                if (fatherInterface.isAssignableFrom(child) && !fatherInterface.equals(child) && !Modifier.isAbstract(child.getModifiers())) {
                    returnList.add(child);
                }
            }
        }
        return returnList;

    }

    /**
     * 把某个包及其子包下面所有标注有annotations里某个annotation的类全部找出来。
     */
    public static List<Class<?>> getAllClassByAnnotations(Set<Class> annotations, String packageName) {

        List<Class<?>> returnList = new ArrayList<>();
        for (Class child : getAllClassInPackage(packageName)) {
            for (Class annotation : annotations) {
                if (child.isAnnotationPresent(annotation)){
                    returnList.add(child);
                    break;
                }
            }
        }
        return returnList;
    }

    /**
     * bean只能定义在Component标注的类中，在这里扫描之。
     */
    public static Map<String, Method> getBeanGenerationMethods(Collection<Class<?>> componentSet, Class<?>... components) {

        Map<String, Method> result = new HashMap<>();
        getBeanMethod(result, componentSet.iterator());
        getBeanMethod(result, Arrays.stream(components).iterator());
        return result;
    }

    private static void getBeanMethod(Map<String, Method> result, Iterator<Class<?>> components) {

        while (components.hasNext()) {
            Class<?> component = components.next();
            for (Method declaredMethod : component.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(ToyBean.class)) {
                    String beanName = getBeanName(declaredMethod);
                    if (result.putIfAbsent(beanName, declaredMethod) != null) {
                        throw new ContextInitException(String.format("在初始化%s时出现了同名Bean,method是%s:%s()"
                                , beanName, declaredMethod.getDeclaringClass().getName(), declaredMethod.getName()));
                    }
                }
            }
        }
    }

    private static String getBeanName(Method declaredMethod) {
        return BeanNameUtils.getBeanId(declaredMethod);
    }

    private static List<Class<?>> getAllClassInPackage(String packageName) {

        try {
            String path = packageName.replace('.', '/');
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classloader.getResources(path);
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String newPath = resource.getFile().replace("%20", " ");
                dirs.add(new File(newPath));
            }
            List<Class<?>> classes = new ArrayList<>();
            for (File directory : dirs) {
                classes.addAll(findClass(directory, packageName));
            }
            return classes;
        } catch (IOException | ClassNotFoundException e) {
            throw new ContextInitException("解析上下文时出错，多半是我的bug，请来打我");
        }

    }


    private static List<Class<?>> findClass(File directory, String packageName)
            throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files != null ? files : new File[0]) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClass(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + "." + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}
