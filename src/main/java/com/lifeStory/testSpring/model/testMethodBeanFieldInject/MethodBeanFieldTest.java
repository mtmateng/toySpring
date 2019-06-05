package com.lifeStory.testSpring.model.testMethodBeanFieldInject;

import lombok.Data;
import org.myToySpring.annotations.ToyBean;
import org.myToySpring.annotations.ToyConfiguration;

@Data
@ToyConfiguration
public class MethodBeanFieldTest {

    @ToyBean
    public NotComponent createNotComponent() {
        return new NotComponent();
    }

}
