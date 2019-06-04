package com.lifeStory.testSpring.model.testDependencyAbsence;

import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class TestConstructDepAbsence {

    private DependencyClass dependencyClass;

    public TestConstructDepAbsence(@ToyAutowired(nullable = true) DependencyClass dependencyClass) {
        this.dependencyClass = dependencyClass;
    }

}
