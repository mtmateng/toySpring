package com.lifeStory.testSpring.model.testDependencyAbsence;

import lombok.Data;
import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyComponent;

@Data
@ToyComponent
public class TestFieldDepAbsence {

    @ToyAutowired(nullable = true)
    DependencyClass dependencyClass;

}
