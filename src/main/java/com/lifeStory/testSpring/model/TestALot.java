package com.lifeStory.testSpring.model;

import com.lifeStory.testSpring.model.testName.TestAutowired;
import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;
import org.myToySpring.annotations.ToyQualifier;

@Data
@ToyComponent
public class TestALot {

    private String value = "测试一大堆自动注入";

    @ToyAutowired
    private TestClassAnnotation testClassAnnotation;

    @ToyAutowired
    @ToyQualifier("hail")
    private TestAutowired testAutowired;

    private TestBean testBean;
    private TestPrimary testPrimary;

    public TestALot(@ToyQualifier("myTestBean") TestBean testBean,
                    TestPrimary testPrimary) {
        this.testBean = testBean;
        this.testPrimary = testPrimary;
    }

}
