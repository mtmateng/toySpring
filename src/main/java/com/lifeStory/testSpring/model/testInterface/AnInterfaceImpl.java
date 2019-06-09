package com.lifeStory.testSpring.model.testInterface;

import lombok.Data;
import org.myToySpring.annotations.ToyBean;
import org.myToySpring.annotations.ToyComponent;
import org.myToySpring.annotations.ToyPrimary;

@Data
@ToyComponent
public class AnInterfaceImpl implements AnInterface {

    private String value = "测试interface";

    @ToyBean
    @ToyPrimary
    public AnInterface createAnInterface() {
        AnInterfaceImpl ret = new AnInterfaceImpl();
        ret.setValue("测试interface primary");
        return ret;
    }

}
