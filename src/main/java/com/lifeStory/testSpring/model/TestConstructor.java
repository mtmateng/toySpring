package com.lifeStory.testSpring.model;

import lombok.Data;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class TestConstructor {

    private String value = "这是测试构造器的必须依赖是否好使";
    private TestClassAnnotation testClassAnnotation;

    public TestConstructor(TestClassAnnotation testClassAnnotation) {
        this.testClassAnnotation = testClassAnnotation;
    }

}
