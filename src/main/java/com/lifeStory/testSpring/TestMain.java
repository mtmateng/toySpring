package com.lifeStory.testSpring;

import com.lifeStory.testSpring.model.*;
import com.lifeStory.testSpring.model.testDependencyAbsence.TestConstructDepAbsence;
import com.lifeStory.testSpring.model.testDependencyAbsence.TestFieldDepAbsence;
import com.lifeStory.testSpring.model.testMethodBeanFieldInject.NotComponent;
import com.lifeStory.testSpring.model.testName.TestAutowired;
import com.lifeStory.testSpring.model.testName.TestBeanName;
import com.lifeStory.testSpring.model.testName.TestComponentName;
import org.myToySpring.annotations.ToyComponentScan;
import org.myToySpring.context.ToySpringBeanContext;
import org.myToySpring.runner.ToySpringRunner;

@ToyComponentScan
public class TestMain {

    public static void main(String[] args) {

        ToySpringBeanContext context = ToySpringRunner.run(TestMain.class, args);
        System.out.println(context.getBean(TestClassAnnotation.class));
        System.out.println(context.getBean(TestConstructor.class));
        System.out.println(context.getBean(TestAutowired.class));
        System.out.println(context.getBean(TestBean.class));
        System.out.println(context.getBean("hail"));
        System.out.println(context.getBean("testClassAnnotation"));
        System.out.println(context.getBean(TestQualifier.class));
        System.out.println(context.getBean(TestMultiConstructor.class));
        System.out.println(context.getBean(TestComponentName.class));
        System.out.println(context.getBean(TestBeanName.class));
        System.out.println(context.getBean(TestFieldDepAbsence.class));
        System.out.println(context.getBean(TestConstructDepAbsence.class));
        System.out.println(context.getBean(TestALot.class));
        System.out.println(context.getBean(NotComponent.class));
        System.out.println(context.getBean(TestToyValue.class));

    }

}
