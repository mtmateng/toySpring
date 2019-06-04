package org.myToySpring.context;

import java.util.*;

/**
 * 这个类用来解析yaml文件。
 * 解析规则是：
 */
public class ToySpringConfigurationContext {

    private final Map<String, String> configurationMap = new HashMap<>();

    public ToySpringConfigurationContext(String[] args) {

        List<String> profiles = getActiveProfile(args);
        parasYaml("application.yaml");


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


}
