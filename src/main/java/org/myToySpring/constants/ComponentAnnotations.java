package org.myToySpring.constants;

import org.myToySpring.annotations.ToyComponent;
import org.myToySpring.annotations.ToyConfiguration;
import org.myToySpring.annotations.ToyRepository;
import org.myToySpring.annotations.ToyService;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentAnnotations {

    public static Set<Class> COMPONENT_ANNOTATIONS = Arrays.stream(new Class[]{
        ToyComponent.class, ToyService.class, ToyRepository.class, ToyConfiguration.class
    }).collect(Collectors.toSet());

}