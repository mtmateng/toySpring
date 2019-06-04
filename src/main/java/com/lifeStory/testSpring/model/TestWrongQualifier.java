package com.lifeStory.testSpring.model;

import lombok.Data;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class TestWrongQualifier {


    private String value = "测试当Qualifier的beanId和类型不匹配时会如何";

    private final TestClassAnnotation testClassAnnotation;

    public TestWrongQualifier(TestClassAnnotation testClassAnnotation) {
        this.testClassAnnotation = testClassAnnotation;
    }

}
