package com.lifeStory.testSpring.model.testLoop;

import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class ClassC {

    @ToyAutowired
    private ClassA classA;

}
