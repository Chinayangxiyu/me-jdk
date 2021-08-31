
## 0、什么是IOC，Spring
## 1、BeanFactory和FactoryBean是是什么

## 2、BeanFactory和ApplicationContext的区别
## 3、BeanDefinition是什么
## 4、构造器注入和setter注入比较
构造器注入优点：  
（1）保证注入bean不可变（setter注入的话，在使用过程中可以去修改bean），  
（2）不为空；
构造器注入缺点：  
（1）需要注入bean太多时不优雅，但是注入的bean太多也说明当前类职责太多，需要重构。
（2）构造器注入无法解决循环依赖（都使用构造器注入），

Setter注入优点：
（1）能处理**循环依赖**。
Setter注入缺点：
（1）使用的地方都需要进行非空。
（2）使用过程不能保证bean的不变性。


The Spring team generally advocates constructor injection as it enables one to implement application components as immutable objects and to ensure that required dependencies are not null. Furthermore constructor-injected components are always returned to client (calling) code in a fully initialized state. As a side note, a large number of constructor arguments is a bad code smell, implying that the class likely has too many responsibilities and should be refactored to better address proper separation of concerns.

Setter injection should primarily only be used for optional dependencies that can be assigned reasonable default values within the class. Otherwise, not-null checks must be performed everywhere the code uses the dependency. One benefit of setter injection is that setter methods make objects of that class amenable to reconfiguration or re-injection later. Management through JMX MBeans is therefore a compelling use case for setter injection.
## 5、@Lazy的作用
延迟加载，Spring容器在启动时beanFactory.preInstantiateSingletons();，如果对应的bean是延迟加载的，则不会去主动的注入到IOC；
但是如果存在非Lazy bean依赖lazy bean的情况，会触发lazy bean的初始化。

