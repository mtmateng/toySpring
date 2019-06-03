## 更新日志及思考

### 2019年5月31日

+ Spring的bean会允许同ID覆盖，可以通过设置使产生同ID的Bean时抛出异常，我们的模块不允许。考虑到下面一段代码，对Spring不熟悉的同志，看到下面这一段都会很诡异，认为会抛异常，但实际上并不会：

  ```java
  @SpringBootApplication
  public class TahitiFileServiceApplication {
  
      public static void main(String[] args) {
          ApplicationContext context = SpringApplication.run(TahitiFileServiceApplication.class, args);
          System.out.println(context.getBean(TestBean.class));
      }
  
      @Bean("testBean")
      public TestBean createBean2() {
          return new TestBean("这是二号Bean");
      }
  
      @Bean("testBean")
      public TestBean createBean() {
          return new TestBean("这是一号Bean");
      }
  }
  ```

  结果是什么，取决于产生Bean的方法声明的顺序。这里面牵扯到很多问题，比如：

  1. 为什么Bean要用ID来标识一个Bean，而不是用类的类型？

     这是因为Spring允许上下文中存在多个同类型的实体。虽然我目前认为，在自己的代码里应该尽量避免这样的情况发生，但作为一个企业级的框架，Spring显然需要支持各种各样的需求。因此，同类型的Bean一定需要被允许同时注册到上下文，也因此必须有个ID。

  2. ID到底怎么生成？

     以注解为主Spring环境下，一个Bean的生成有两种方式，一种是标注了`@Component`系列注解(包括`@Service`、`@Controller`、`@Repository`等)的类，将会自动生成一个Bean注册到上下文中，一种是在标注了`@Configuration`的类里面，使用`@Bean`标注的方法，其返回值将会被作为一个Bean注册到上下文中。

     对应的，如果是`@Component`系列注解产生的Bean，其ID以该系列注解的`Value`参数决定，类似`@Component(value="beanId")`这样子，如果该`value`值不存在，则以该Bean的`simpleClassName`作为ID，首字母会自动小写。

     如果是`@Bean`标注的方法生成的Bean，其ID以`@Bean`注解的`name`参数决定，类似于`@Bean(nema="beanId")`这样子，如果该`name`值不存在，则以该方法的方法名作为BeanID。