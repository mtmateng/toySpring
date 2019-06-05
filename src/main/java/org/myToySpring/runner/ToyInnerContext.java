package org.myToySpring.runner;

import lombok.Getter;
import org.myToySpring.annotations.*;
import org.myToySpring.constants.ComponentAnnotations;
import org.myToySpring.constants.IllegalBeanType;
import org.myToySpring.context.ToySpringConfigurationContext;
import org.myToySpring.exceptions.ContextInitException;
import org.myToySpring.helper.BeanProperty;
import org.myToySpring.utils.BeanNameUtils;
import org.myToySpring.utils.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 一个在初始化时内部使用的Context。作用是把每个容器管理的Bean的属性、依赖关系理清楚
 * 方法是递归扫描包，当指示这是一个ManagedBean的注解被发现后，我们就将这个Bean的相关属性注册进来
 * 因为当第一遍扫描时，某个Bean的依赖Bean可能还没扫描到，无法决定其属性，因此实际需要扫描两遍
 * 第一遍建立基本信息，第二遍开始构建BeanProperty，即真正的bean属性和依赖关系类。
 */
class ToyInnerContext {

    @Getter
    private final Map<String, BeanProperty> beanId2BeanProperty = new HashMap<>();
    private final Map<String, Class> beanId2BeanType = new HashMap<>();
    private final Map<String, Method> beanId2BeanMethod = new HashMap<>();
    private final Map<Class, List<String>> beanType2BeanIds = new HashMap<>();
    private final Map<Class, String> beanType2Primary = new HashMap<>();
    private final ToySpringConfigurationContext configurationContext;

    private static final String NULLABLE_BEAN_ID = "__nullableBeans__";
    private static final Set<Class> CONFIGURABLE_PARAMETER_CLASS = new HashSet<>(Arrays.asList(
            int.class, Integer.class, float.class, Float.class, boolean.class, Boolean.class,
            double.class, Double.class, List.class, ArrayList.class, String.class,
            Long.class, long.class
    ));

    ToyInnerContext(Class<?> mainClass, ToySpringConfigurationContext configurationContext) {

        scanAndRegisterBeans(mainClass);
        this.configurationContext = configurationContext;

    }

    private void scanAndRegisterBeans(Class<?> mainClass) {

        List<Class<?>> classes = ClassUtils.getAllClassByAnnotations(ComponentAnnotations.COMPONENT_ANNOTATIONS, getComponentScanPackageName(mainClass));

        // 第一遍扫描，将所有的类的类型元数据先注册到上下文里
        // 先把所有的标注了@Component类注解的类都扫描到上下文里
        for (Class<?> aClass : classes) {
            registerBean(getManagedBeanId(aClass), aClass, aClass.isAnnotationPresent(ToyPrimary.class));
        }

        // 再从这些@Component类中找出带有@Bean标注的方法
        Map<String, Method> beanId2BeanGenerationMethod = ClassUtils.getBeanGenerationMethods(classes, mainClass);
        for (String beanId : beanId2BeanGenerationMethod.keySet()) {
            registerBeanMethod(beanId, beanId2BeanGenerationMethod.get(beanId));
            registerBean(beanId, getReturnBeanType(beanId2BeanGenerationMethod.get(beanId)),
                    beanId2BeanGenerationMethod.get(beanId).isAnnotationPresent(ToyPrimary.class));
        }

        //开始第二遍扫描，构建BeanProperty
        beanId2BeanType.forEach(this::registerBeanPropertyByClass);
        beanId2BeanMethod.forEach(this::registerBeanPropertyByMethod);

        //接下来生成图，生成bean，完成这个拼图
        List<String> sortedBeanIds = buildDAG();
        instanceBean(sortedBeanIds);

    }

    /**
     * 按顺序实例化Bean，分为两步，第一步是先实例化、同时注入必须依赖
     * 第二步是把非必须依赖注入，即Field依赖。
     */
    private void instanceBean(List<String> sortedBeanIds) {

        // 这一步是实例化所有的Bean
        for (String beanId : sortedBeanIds) {
            if (beanId.equals(NULLABLE_BEAN_ID)) {
                continue;
            }
            BeanProperty beanProperty = beanId2BeanProperty.get(beanId);
            switch (beanProperty.getBeanGenerateType()) {
                case Component:
                    instanceBeanByConstructor(beanProperty);
                    break;
                case MethodBean:
                    instanceBeanByMethodBean(beanProperty);
                    break;
                default:
                    throw new ContextInitException("beanProperty的type有误，请检查");
            }
        }
        // 注入非必须依赖
        for (String beanId : sortedBeanIds) {
            BeanProperty beanProperty = beanId2BeanProperty.get(beanId);
            beanProperty.getField2Dependencies().forEach((k, v) -> {
                try {
                    Field field = beanProperty.getBeanType().getDeclaredField(k);
                    field.setAccessible(true);
                    field.set(beanProperty.getBean(), beanId2BeanProperty.get(v).getBean());
                } catch (Exception e) {
                    throw new ContextInitException(String.format("为%s注入非必须依赖:%s时出错", beanId, k), e);
                }
            });
        }

    }

    private void instanceBeanByConstructor(BeanProperty beanProperty) {
        Object[] params = beanProperty.getNecessaryDependencies().stream()
                .map(beanId -> beanId2BeanProperty.get(beanId).getBean()).toArray();
        try {
            beanProperty.setBean(beanProperty.getConstructor().newInstance(params));
        } catch (Exception e) {
            throw new ContextInitException(String.format("实例化Bean %s:%s 时出错", beanProperty.getBeanId(), beanProperty.getBeanType().getName()));
        }
    }

    private void instanceBeanByMethodBean(BeanProperty beanProperty) {
        Object[] params = beanProperty.getNecessaryDependencies().stream().filter(s -> !beanProperty.getMethodRestBeanId().equals(s))
                .map(beanId -> beanId2BeanProperty.get(beanId).getBean()).toArray();
        try {
            beanProperty.setBean(beanProperty.getMethod()
                    .invoke(beanId2BeanProperty.get(beanProperty.getMethodRestBeanId()).getBean(), params));
        } catch (Exception e) {
            throw new ContextInitException(String.format("实例化Bean %s:%s 时出错", beanProperty.getBeanId(), beanProperty.getBeanType().getName()), e);
        }
    }

    /**
     * 将需要管理的类构建成一个有向无环图，之后顺序化为一个有序集合，一个一个初始化。
     * 这样当我们初始化某个类时，我们就能确信它的依赖类都已经初始化好了
     * @return 确保被依赖类在前的一个有序列表，这是Bean的Id
     */
    private List<String> buildDAG() {

        Map<String, Set<String>> graph = new HashMap<>();   //value 为 key 直接依赖的类，全部为ID
        for (String beanId : beanId2BeanProperty.keySet()) {
            graph.put(beanId, new HashSet<>(beanId2BeanProperty.get(beanId).getNecessaryDependencies()));
        }

        List<String> sortedClasses = new ArrayList<>();
        Set<String> zeroInDegreeClasses = new HashSet<>();
        while (!graph.isEmpty()) {
            for (String beanId : graph.keySet()) {
                if (graph.get(beanId).isEmpty()) {
                    zeroInDegreeClasses.add(beanId);
                    sortedClasses.add(beanId);
                    removeDependencyFromGraph(beanId, graph);
                }
            }
            if (zeroInDegreeClasses.isEmpty()) {
                throw new ContextInitException(String.format("组建Bean时发生环装依赖，请检查；相关类是%s", String.join(",", graph.keySet())));
            }
            for (String shouldRemove : zeroInDegreeClasses) {
                graph.remove(shouldRemove);
            }
            zeroInDegreeClasses.clear();
        }
        return sortedClasses;

    }

    private static void removeDependencyFromGraph(String classId, Map<String, Set<String>> graph) {

        for (String key : graph.keySet()) {
            graph.get(key).remove(classId);
        }

    }

    private Class getReturnBeanType(Method method) {
        IllegalBeanType.checkBeanTypeLegallity(method.getReturnType());
        return method.getReturnType();
    }

    private void registerBeanMethod(String beanId, Method method) {

        if (beanId.equals(NULLABLE_BEAN_ID)) {
            throw new RuntimeException(NULLABLE_BEAN_ID + "为系统保留的BeanName，请勿使用");
        }
        if (beanId2BeanMethod.containsKey(beanId)) {
            throw new ContextInitException(String.format("name为%s的bean已经存在，其类型为%s",
                    beanId, beanId2BeanType.get(beanId)));
        }
        beanId2BeanMethod.put(beanId, method);
    }

    private void registerBean(String beanId, Class aClass, Boolean primary) {

        if (beanId.equals(NULLABLE_BEAN_ID)) {
            throw new RuntimeException(NULLABLE_BEAN_ID + "为系统保留的BeanName，请勿使用");
        }
        IllegalBeanType.checkBeanTypeLegallity(aClass);
        if (beanId2BeanType.containsKey(beanId)) {
            throw new ContextInitException(String.format("尝试注册name为%s，类型为%s的类型，但已有同名类，其类型为%s，请检查",
                    beanId, aClass.getName(), beanId2BeanType.get(beanId).getName()));
        }
        beanId2BeanType.put(beanId, aClass);
        beanType2BeanIds.putIfAbsent(aClass, new ArrayList<>());
        beanType2BeanIds.get(aClass).add(beanId);
        registerPrimary(primary, aClass, beanId);

    }

    /**
     * class上直接标注Component生成的Bean，其依赖通过构造函数来检查依赖项。如果只有一个构造函数，将使用之。
     * 如果有多个，则选择有@ToyAutowired标注的，如果有多个标注或者均无标注，则抛异常
     */
    private void registerBeanPropertyByClass(String beanId, Class domainClass) {

        BeanProperty beanProperty = new BeanProperty();
        Constructor constructor = getTheConstructor(domainClass);
        beanProperty.setBeanGenerateType(BeanProperty.BeanGenerateType.Component);
        beanProperty.setConstructor(constructor);
        beanProperty.setBeanId(BeanNameUtils.getComponentId(domainClass));
        beanProperty.setBeanType(domainClass);
        beanProperty.setNecessaryDependencies(getConstructorDependency(constructor));
        Map<String, String> field2Dependencies = getFieldDependencies(domainClass);
        beanProperty.setField2Dependencies(field2Dependencies);
        beanProperty.setPrimary(domainClass.isAnnotationPresent(ToyPrimary.class));
        beanId2BeanProperty.put(beanId, beanProperty);

    }

    private void registerBeanPropertyByMethod(String beanId, Method method) {

        BeanProperty beanProperty = new BeanProperty();
        beanProperty.setBeanId(BeanNameUtils.getBeanId(method));
        beanProperty.setBeanGenerateType(BeanProperty.BeanGenerateType.MethodBean);
        beanProperty.setBeanType(method.getReturnType());
        List<String> necessaryDependencies = Arrays.stream(method.getParameters())
                .map(parameter -> {
                    if (parameter.isAnnotationPresent(ToyQualifier.class)) {
                        if (parameter.getType() == beanId2BeanType.get(beanId)) {
                            return parameter.getAnnotation(ToyQualifier.class).value();
                        } else {
                            throw new ContextInitException(String.format("%s的参数类型为%s的参数：%s试图通过ToyQualifier组装Id为%s的bean，但该bean类型为%s",
                                    method.getName(), parameter.getType().getName(), parameter.getName(), beanId, beanId2BeanType.get(beanId)));
                        }
                    } else {
                        return getBeanIdByClassType(parameter.getType(), method.getDeclaringClass().getName() + "." + method.getName(),
                                parameter.isAnnotationPresent(ToyAutowired.class) && parameter.getAnnotation(ToyAutowired.class).nullable());
                    }
                }).collect(Collectors.toList());
        // 还依赖于该方法声明的那个Class
        String methodRestBeanId = getManagedBeanId(method.getDeclaringClass());
        necessaryDependencies.add(methodRestBeanId);
        beanProperty.setNecessaryDependencies(necessaryDependencies);
        beanProperty.setMethodRestBeanId(methodRestBeanId);
        Map<String, String> field2Dependencies = getFieldDependencies(method.getReturnType());
        beanProperty.setField2Dependencies(field2Dependencies);
        beanProperty.setMethod(method);
        beanProperty.setPrimary(method.isAnnotationPresent(ToyPrimary.class));
        beanId2BeanProperty.put(beanId, beanProperty);

    }

    private Map<String, String> getFieldDependencies(Class<?> domainClass) {
        Map<String, String> field2Dependencies = new HashMap<>();
        for (Field declaredField : domainClass.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(ToyAutowired.class)) {
                if (declaredField.isAnnotationPresent(ToyQualifier.class)) {
                    String beanId = declaredField.getAnnotation(ToyQualifier.class).value();
                    if (declaredField.getType() == beanId2BeanType.get(beanId)) {
                        field2Dependencies.put(declaredField.getName(), declaredField.getAnnotation(ToyQualifier.class).value());
                    } else {
                        throw new ContextInitException(String.format("%s的参数类型为%s的参数：%s试图通过ToyQualifier组装Id为%s的bean，但该bean类型为%s",
                                declaredField.getName(), declaredField.getType().getName(), declaredField.getName(), beanId, beanId2BeanType.get(beanId)));
                    }
                } else {
                    field2Dependencies.put(declaredField.getName(),
                            getBeanIdByClassType(declaredField.getType(), domainClass.getName(),
                                    declaredField.isAnnotationPresent(ToyAutowired.class) && declaredField.getAnnotation(ToyAutowired.class).nullable()));
                }
            }
        }
        return field2Dependencies;
    }

    private String getBeanIdByClassType(Class<?> fieldClass, String domainBeanName, boolean nullable) {

        if (beanType2Primary.get(fieldClass) != null) {
            return beanType2Primary.get(fieldClass);
        } else if (beanType2BeanIds.get(fieldClass) == null || beanType2BeanIds.get(fieldClass).size() == 0) {
            // 缺少了一些对象，如果是nullable的，我们就创建一个"nullable"的beanProperty，其bean为null
            if (nullable && beanId2BeanProperty.get(NULLABLE_BEAN_ID) == null) {
                BeanProperty beanProperty = new BeanProperty();
                beanProperty.setPrimary(false);
                beanProperty.setBean(null);
                beanProperty.setBeanId(NULLABLE_BEAN_ID);
                beanProperty.setNecessaryDependencies(Collections.emptyList());
                beanProperty.setField2Dependencies(Collections.emptyMap());
                beanId2BeanProperty.put(NULLABLE_BEAN_ID, beanProperty);
                return NULLABLE_BEAN_ID;
            } else {
                throw new ContextInitException(String
                        .format("构建%s时尝试寻找类型为%s的类，但未找到", domainBeanName, fieldClass.getName()));
            }
        } else if (beanType2BeanIds.get(fieldClass).size() == 1) {
            return beanType2BeanIds.get(fieldClass).get(0);
        } else {
            throw new ContextInitException(String
                    .format("构建%s时尝试寻找类型为%s的类，找到了不止一个，请考虑通过@ToyQualifier指定需要注入哪个bean，或使用@ToyPrimary", domainBeanName, fieldClass.getName()));
        }
    }

    /**
     * 所有的bean在容器中都有一个Id，Spring称为name，如果指定了name，比如通过@Bean指定，或者在@Component及其子注解中指定
     * 则该name为指定的名字，如果未指定，则为该类的简单名，首字母小写。
     */
    private static String getManagedBeanId(Class<?> aClass) {

        return BeanNameUtils.getComponentId(aClass);

    }

    /**
     * 如果ToyComponentScan的packageName有值，那么将使用这个值，如果没有，使用mainClass所在包为包扫描的根路径
     * 这似乎也是Spring的默认策略，因此有时候发现明明标注了@Component等方法，却扫描不到东西，可以先检查一下MainClass
     * 的路径是否正确，是否放在了一个不正确的子路径下。
     * @param mainClass 标注了ToyComponentScan的那个类
     * @return 需要扫描的packageName
     */
    private static String getComponentScanPackageName(Class<?> mainClass) {
        return mainClass.getAnnotation(ToyComponentScan.class).packageName().equals("")
                ? mainClass.getPackage().getName()
                : mainClass.getAnnotation(ToyComponentScan.class).packageName();
    }

    /**
     * 此处的策略是：仅查看构造函数的参数，如果只有一个构造函数，那就看构造函数的类型，如果有多个构造函数，那就看是否有一个标注了@Autowired
     * 如果多个标注了Autowired，或者都没有标注Autowired，就出错。
     */
    private List<String> getConstructorDependency(Constructor constructor) {

        List<String> dependencies = new ArrayList<>();
        Parameter[] parameters = constructor.getParameters();
        for (Parameter parameter : parameters) {
            if (CONFIGURABLE_PARAMETER_CLASS.contains(parameter.getType())) {
                if (parameter.isAnnotationPresent(ToyValue.class)) {
                    continue;
                } else {
                    throw new ContextInitException(String.format("%s的构造函数中的参数%s不支持注入Bean的类型，请考虑用@ToyValue导入",
                            constructor.getDeclaringClass(), parameter.getName()));
                }
            }
            dependencies.add(getParameterRequireBeanName(parameter, parameter.isAnnotationPresent(ToyAutowired.class) && parameter.getAnnotation(ToyAutowired.class).nullable()));
        }
        return dependencies;

    }

    /**
     * 解析参数需要的Bean的ID，策略是，如果没有名字，那么就查询
     */
    private String getParameterRequireBeanName(Parameter parameter, boolean nullable) {

        String beanId;
        if (parameter.isAnnotationPresent(ToyQualifier.class)) {        //如果有Qualifier标注，那么用指定的ID
            beanId = parameter.getAnnotation(ToyQualifier.class).value();
            if (parameter.getType() == beanId2BeanType.get(beanId)) {
                return parameter.getAnnotation(ToyQualifier.class).value();
            } else {
                throw new ContextInitException(String.format("类型为%s的参数：%s试图通过ToyQualifier组装Id为%s的bean，但该bean类型为%s",
                        parameter.getType().getName(), parameter.getName(), beanId, beanId2BeanType.get(beanId)));
            }
        } else if (beanType2Primary.get(parameter.getType()) != null) {     //或者用primary类的Id
            beanId = beanType2Primary.get(parameter.getType());
        } else {       //接下来去找class对应的ID，当然了，如果此时同一个类还有多个Bean，那就会抛异常咯
            beanId = getNameByBeanType(parameter.getType(), nullable);
        }

        if (!beanId.equals(NULLABLE_BEAN_ID) && beanId2BeanType.get(beanId) == null) {       //根本没有这个Id的bean
            throw new ContextInitException(String.format("尝试寻找Id为%s的类，但并不存在", beanId));
        }
        return beanId;

    }

    private String getNameByBeanType(Class aClass, boolean nullable) {
        if (beanType2BeanIds.get(aClass) == null || beanType2BeanIds.get(aClass).isEmpty()) {
            if (nullable) {
                return NULLABLE_BEAN_ID;
            } else {
                throw new ContextInitException(String.format("context中没有定义%s", aClass.getName()));
            }
        } else if (beanType2BeanIds.get(aClass).size() > 1) {
            throw new ContextInitException(String.format("context中发现多个%s", aClass.getName()));
        } else return beanType2BeanIds.get(aClass).get(0);
    }

    private static Constructor<?> getTheConstructor(Class<?> aClass) {
        Constructor<?>[] constructors = aClass.getConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        List<Constructor<?>> autowiredConstructors = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(ToyAutowired.class) != null) {
                autowiredConstructors.add(constructor);
            }
        }
        if (autowiredConstructors.size() != 1) {
            throw new ContextInitException(String.format(
                    "发现%s有多个构造函数，无法确认使用哪一个，请在方法上标注@ToyAutowired", aClass.getName()));
        }
        return autowiredConstructors.get(0);
    }

    private void registerPrimary(Boolean primary, Class aClass, String beanId) {

        if (!primary) {
            return;
        }
        if (beanType2Primary.containsKey(aClass)) {
            throw new ContextInitException(String.format("为类型为%s的标注了两个Primary，请检查", aClass.getName()));
        }
        beanType2Primary.put(aClass, beanId);

    }

}
