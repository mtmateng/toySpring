package com.lifeStory.testSpring.model;

import com.lifeStory.testSpring.model.testName.TestBeanMethod;
import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class TestMultiConstructor {

    private String value = "我是测试多构造函数的情况的";
    private TestBeanMethod testBeanMethod;

    @ToyAutowired
    public TestMultiConstructor() {

        TestBeanMethod testBeanMethod = new TestBeanMethod();
        testBeanMethod.setValue("我是测试BeanMethod是否能产生bean的二号");
        this.testBeanMethod = testBeanMethod;

    }

    public TestMultiConstructor(TestBeanMethod testBeanMethod) {

        this.testBeanMethod = testBeanMethod;
    }


}
