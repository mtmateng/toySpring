package com.lifeStory.testSpring.model.testName;

import com.lifeStory.testSpring.model.TestClassAnnotation;
import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent("hail")
public class TestAutowired {

    private String value = "这是测试Autowired是否好使";
    @ToyAutowired
    private TestClassAnnotation testClassAnnotation;

}
