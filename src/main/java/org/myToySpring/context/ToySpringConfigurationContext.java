package org.myToySpring.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.myToySpring.exceptions.ContextInitException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 这个类用来解析yaml文件。
 * 解析规则是：
 *  1. 先解析application.yaml文件，如果没有，什么也不做
 *  2. 然后解析其他activeProfile指定的配置文件，直接覆盖同名参数
 *  3. 然后解析args中的参数，直接覆盖同名参数
 * 关于profiles的确定：
 *  1. 先读取application.yml文件中的toySpring.active.profile参数
 *  2. 从args中读取toySpring.active.profile，如果没有，使用application.yml中的参数，如果有，覆盖application.yml的配置
 *  3. 在activeProfile中发现的toySpring.active.profile参数，直接忽略
 * 实际的Spring解析十分复杂，还支持环境变量等，总共有10个数据源，一层一层覆盖。而且支持yaml、xml、yml、properties各种文件后缀
 * 的解析，我这里只实现了yml文件，即后缀是yml的文件
 */
public class ToySpringConfigurationContext {

    private final Map<String, Object> configurationMap = new HashMap<>();
    private static final YAMLFactory yamlFactory = new YAMLFactory();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Set<Class> CONFIGURABLE_PARAMETER_CLASS = new HashSet<>(Arrays.asList(
        int.class, Integer.class, float.class, Float.class, boolean.class, Boolean.class,
        double.class, Double.class, String.class,
        Long.class, long.class
    ));

    public ToySpringConfigurationContext(String[] args) {

        parseYaml("");

        List<String> profiles = getActiveProfile(args);
        if (profiles.isEmpty() && configurationMap.get("toySpring.profiles.active") != null) {
            profiles = Arrays.asList(configurationMap.get("toySpring.profiles.active").toString().split(","));
        }

        // 解析其他activeProfile的配置文件
        for (String profile : profiles) {
            parseYaml(profile);
        }

        // 解析args中传入的参数，进行进一步覆盖
        parseArgs(args);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, Class<T> tClass, Type type) {

        if (configurationMap.get(key) == null) {
            throw new ContextInitException(String.format("尝试寻找key为%s的配置参数，但未找到", key));
        }

        if (List.class.isAssignableFrom(tClass) || ArrayList.class.isAssignableFrom(tClass)) {
            Class payloadClass = getPayloadType(type, key);
            ArrayList value = new ArrayList();
            if (configurationMap.get(key) instanceof ArrayList) {
                for (Object object : (ArrayList) configurationMap.get(key)) {
                    value.add(getRealValue(object, payloadClass, key));
                }
                return (T) value;
            } else throw new ContextInitException(String.format("key为%s的配置参数不能被转化为%s", key, tClass.getName()));
        }
        return (T) getRealValue(configurationMap.get(key), tClass, key);

    }

    private Object getRealValue(Object object, Class tClass, String key) {

        try {
            if (tClass == String.class) {
                return object.toString();
            } else if (tClass == int.class || tClass == Integer.class) {
                return Integer.valueOf(object.toString());
            } else if (tClass == boolean.class || tClass == Boolean.class) {
                return Boolean.valueOf(object.toString());
            } else if (tClass == float.class || tClass == Float.class) {
                return Float.valueOf(object.toString());
            } else if (tClass == double.class || tClass == Double.class) {
                return Double.valueOf(object.toString());
            } else if (tClass == long.class || tClass == Long.class) {
                return Long.valueOf(object.toString());
            }
        } catch (Exception e) {
            throw new ContextInitException(String.format("key为%s的配置参数，值为%s，不能被转化为%s", key, object, tClass.getName()), e);
        }

        throw new ContextInitException(String.format("key为%s的配置参数，值为%s，不能被转化为%s", key, object, tClass.getName()));

    }

    private Class getPayloadType(Type type, String key) {

        if (type instanceof ParameterizedType) {
            Type paramType = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (!(paramType instanceof Class) || !CONFIGURABLE_PARAMETER_CLASS.contains(paramType)) {
                throw new RuntimeException(String.format("key为%s的参数，其List的参数化类型%s不能解析", key, paramType.getTypeName()));
            }
            return (Class) paramType;
        } else if (type instanceof Class) {
            return Object.class;
        }
        return Object.class;
    }

    private void parseArgs(String[] args) {

        for (String arg : args) {
            String[] kv;
            if (arg.startsWith("--") && (kv = arg.replaceFirst("--", "").split("=")).length == 2) {
                configurationMap.put(kv[0], kv[1]);
            }
        }

    }

    private void parseYaml(String profileName) {

        File profileFile = getProfileFile(profileName);
        if (profileFile == null) {
            return;
        }
        try {
            YAMLParser yamlParser = yamlFactory.createParser(new FileInputStream(profileFile));
            JsonNode node = mapper.readTree(yamlParser);
            TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
            Map<String, Object> config = mapper.readValue(treeTraversingParser, new TypeReference<Map<String, Object>>() {
            });
            config = expandMap(config);
            Map<String, Object> singleConfig = new HashMap<>();
            mergeMapKey("", config, singleConfig);
            singleConfig.forEach(configurationMap::put);
        } catch (IOException e) {
            throw new ContextInitException(String.format("读取配置文件%s时出错", profileFile.getAbsolutePath()));
        }

    }

    @SuppressWarnings("unchecked")
    private void mergeMapKey(String prefix, Map<String, Object> config, Map<String, Object> singleConfig) {

        for (String key : config.keySet()) {
            String finalKey = prefix.equals("") ? key : prefix + "." + key;
            if (singleConfig.get(finalKey) != null) {
                throw new ContextInitException(String.format("%s这个配置项已存在，请检查", finalKey));
            }
            if (config.get(key) instanceof Map) {
                mergeMapKey(finalKey, (Map<String, Object>) config.get(key), singleConfig);
            } else {
                singleConfig.put(finalKey, config.get(key));
            }
        }

    }

    private File getProfileFile(String profileName) {

        String ymlFile = "application" + (profileName.equals("") ? "" : "-" + profileName) + ".yml";
        if (this.getClass().getClassLoader().getResource(ymlFile) == null) {
            return null;
        } else {
            return new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(ymlFile)).getFile());
        }

    }

    private List<String> getActiveProfile(String[] args) {
        List<String> ret = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("--toySpring.profiles.active=")) {
                ret.addAll(Arrays.asList(arg.replace("--toySpring.profiles.active=", "").split(",")));
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> expandMap(Map<String, Object> config) {

        Map<String, Object> newConfig = new HashMap<>();
        for (String key : config.keySet()) {
            if (config.get(key) instanceof Map) {
                createPath(newConfig, key.split("\\."), expandMap((Map<String, Object>) config.get(key)));
            } else {
                createPath(newConfig, key.split("\\."), config.get(key));
            }
        }
        return newConfig;

    }

    @SuppressWarnings("unchecked")
    private static void createPath(Map<String, Object> newConfig, String[] subKeys, Object payLoad) {

        try {
            for (int i = 0; i < subKeys.length - 1; ++i) {
                newConfig.putIfAbsent(subKeys[i], new HashMap<String, Object>());
                newConfig = (Map<String, Object>) newConfig.get(subKeys[i]);
            }
        } catch (Exception e) {
            throw new ContextInitException(String.format("解析%s参数时发生错误，请检查是否发生路径覆盖", String.join(".", subKeys)));
        }
        if (newConfig.get(subKeys[subKeys.length - 1]) == null) {
            newConfig.put(subKeys[subKeys.length - 1], payLoad);
        } else if (newConfig.get(subKeys[subKeys.length - 1]) instanceof Map && payLoad instanceof Map) {
            mergeMap((Map<String, Object>) newConfig.get(subKeys[subKeys.length - 1]), (Map<String, Object>) payLoad, subKeys);
        } else {
            throw new ContextInitException(String.format("解析%s参数时发生错误，请检查是否发生路径覆盖", String.join(".", subKeys)));
        }

    }

    @SuppressWarnings("unchecked")
    private static void mergeMap(Map<String, Object> target, Map<String, Object> source, String[] subKeys) {

        for (String key : source.keySet()) {
            if (target.get(key) == null) {
                target.put(key, source.get(key));
            } else if (target.get(key) instanceof Map && source.get(key) instanceof Map) {
                mergeMap((Map<String, Object>) target.get(key), (Map<String, Object>) source.get(key), subKeys);
            } else {
                throw new ContextInitException(String.format("解析%s参数时发生错误，请检查是否发生路径覆盖", String.join(".", subKeys)));
            }
        }

    }

}
