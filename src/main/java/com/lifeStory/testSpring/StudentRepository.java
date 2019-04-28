package com.lifeStory.testSpring;

import com.lifeStory.testSpring.model.Student;
import org.myToySpring.annotations.ToyRepository;

import javax.sql.DataSource;

@ToyRepository
public class StudentRepository {

    private DataSource dataSource;

    // todo
    public Student findById() {
        Student student = new Student();
        student.setId(1);
        student.setAge(20);
        student.setGender("Male");
        student.setName("小马");
        return student;
    }


}
