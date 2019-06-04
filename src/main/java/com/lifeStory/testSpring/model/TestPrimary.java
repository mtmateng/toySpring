package com.lifeStory.testSpring.model;

import lombok.Data;
import org.myToySpring.annotations.ToyBean;
import org.myToySpring.annotations.ToyConfiguration;
import org.myToySpring.annotations.ToyPrimary;

@Data
@ToyConfiguration
public class TestPrimary {

    private String value = "我是测试Primary是不是好使的";

    @ToyBean
    @ToyPrimary
    public TestClassAnnotation createTestAnnotation() {
        TestClassAnnotation testClassAnnotation = new TestClassAnnotation();
        testClassAnnotation.setValue("我是测试@ToyComponet系列注解是否能被正确扫描二号");
        return testClassAnnotation;
    }

}
