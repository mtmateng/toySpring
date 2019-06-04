package com.lifeStory.testSpring.model.testLoop;

import lombok.Data;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class ClassA {

    private ClassB classB;

    public ClassA(ClassB classB) {
        this.classB = classB;
    }

}
