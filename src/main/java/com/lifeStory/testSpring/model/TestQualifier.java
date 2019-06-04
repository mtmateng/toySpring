package com.lifeStory.testSpring.model;

import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;
import org.myToySpring.annotations.ToyQualifier;

@Data
@ToyComponent
public class TestQualifier {

    @ToyAutowired
    @ToyQualifier("testClassAnnotation")
    private TestClassAnnotation testClassAnnotation;

    private String value = "这是测试Qualifier是不是好使的";

}
