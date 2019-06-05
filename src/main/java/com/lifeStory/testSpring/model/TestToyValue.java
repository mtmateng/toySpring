package com.lifeStory.testSpring.model;

import lombok.Data;
import org.myToySpring.annotations.ToyComponent;
import org.myToySpring.annotations.ToyValue;

import java.util.List;

@Data
@ToyComponent
public class TestToyValue {

    @ToyValue("spring.testParam1")
    private String testParam1;

    private TestConstructor testConstructor;
    private TestClassAnnotation testClassAnnotation;
    private String deepParam;
    private List<String> list;
    private boolean haoma;
    private int testInt;


    public TestToyValue(TestConstructor testConstructor,
                        @ToyValue("spring.testParam2.deepParam") String deepParam,
                        @ToyValue("test.hello.list") List<String> list,
                        @ToyValue("test.hello.haoma") boolean haoma,
                        TestClassAnnotation testClassAnnotation,
                        @ToyValue("test.hello.nibuhao") Integer testInt) {
        this.testClassAnnotation = testClassAnnotation;
        this.testConstructor = testConstructor;
        this.deepParam = deepParam;
        this.list = list;
        this.haoma = haoma;
        this.testInt = testInt;
    }

}
