package com.lifeStory.testSpring.model.testInterface;

import lombok.Data;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class InjectInterface {

    private final AnInterface anInterface;

    public InjectInterface(AnInterface anInterface) {
        this.anInterface = anInterface;
    }

}
