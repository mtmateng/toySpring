package com.lifeStory.testSpring.model;

import lombok.Data;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class TestClassAnnotation {

    private String value = "我是测试@ToyComponet系列注解是否能被正确扫描";

}
