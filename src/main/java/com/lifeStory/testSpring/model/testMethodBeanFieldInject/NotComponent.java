package com.lifeStory.testSpring.model.testMethodBeanFieldInject;

import com.lifeStory.testSpring.model.TestClassAnnotation;
import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;

@Data
public class NotComponent {

    private final String value = "测试非Component、通过Method产生的Bean，能不能自动注入依赖";

    @ToyAutowired
    private TestClassAnnotation testClassAnnotation;

}
