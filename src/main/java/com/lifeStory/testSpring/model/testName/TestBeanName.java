package com.lifeStory.testSpring.model.testName;

import com.lifeStory.testSpring.model.TestBean;
import lombok.Data;
import org.myToySpring.annotations.ToyComponent;
import org.myToySpring.annotations.ToyQualifier;

@Data
@ToyComponent
public class TestBeanName {

    private String value = "这是测试MethodBean的name是不是好使的";
    private TestBean testBean;

    public TestBeanName(@ToyQualifier("myTestBean") TestBean testBean) {
        this.testBean = testBean;
    }

}
