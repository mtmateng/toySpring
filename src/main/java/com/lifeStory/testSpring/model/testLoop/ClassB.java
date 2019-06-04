package com.lifeStory.testSpring.model.testLoop;

import lombok.Data;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class ClassB {

    private ClassC classC;

    public ClassB(ClassC classC) {
        this.classC = classC;
    }

}
