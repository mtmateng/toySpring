package com.lifeStory.testSpring.model.testName;

import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;
import org.myToySpring.annotations.ToyQualifier;

@Data
@ToyComponent
public class TestComponentName {

    private String value = "我是测试ComponentName的";

    @ToyAutowired
    @ToyQualifier("hail")
    TestAutowired testAutowired;

}
