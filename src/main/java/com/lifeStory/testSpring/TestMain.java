package com.lifeStory.testSpring;

import org.myToySpring.annotations.ToyComponentScan;
import org.myToySpring.annotations.ToyQualifier;
import org.myToySpring.context.ToySpringContext;
import org.myToySpring.runner.ToySpringRunner;

@ToyComponentScan
public class TestMain {

    public static void main(String[] args) {
        ToySpringContext context = ToySpringRunner.run(TestMain.class, args);
    }

}
