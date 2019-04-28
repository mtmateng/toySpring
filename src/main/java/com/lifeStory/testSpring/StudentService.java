package com.lifeStory.testSpring;

import org.myToySpring.annotations.ToyAutowired;
import org.myToySpring.annotations.ToyService;

@ToyService
public class StudentService {

    @ToyAutowired
    private StudentRepository studentRepository;




}
