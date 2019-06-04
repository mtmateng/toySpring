package com.lifeStory.testSpring.model.testName;

import com.lifeStory.testSpring.model.TestBean;
import lombok.Data;
import org.myToySpring.annotations.ToyBean;
import org.myToySpring.annotations.ToyConfiguration;

@Data
@ToyConfiguration
public class TestBeanMethod {

    private String value = "我是测试BeanMethod是否能产生bean的";

    @ToyBean(name = "myTestBean")
    public TestBean createTestBean() {
        return new TestBean();
    }

}
