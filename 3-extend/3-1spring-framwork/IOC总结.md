

# 一、SpringIOC相关类说明
## BeanFactory
概述：Spring bean容器根接口；重要子接口如下  
HierarchicalBeanFactory：具有层级关系的容器。  
ListableBeanFactory：以列表的结构管理所有的bean。  
AutowireCapableBeanFactory：自动装配功能的beanFactory。  
DefaultListableBeanFactory：Spring默认的beanFactory实现。  
ApplicationContext：在容器基础上扩展了国际化、资源管理、事件监听等功能


## BeanDefinition
概述：bean定义的抽象。  
RootBeanDefinition：合并后的bean定义  
GenericBeanDefinition：通用的bean定义
### 来源如下
|                     | 基于资源文件             | 显示注册                        | 自动扫描                         | @Bean注册                                        |
|:--------------------|:------------------------|:-------------------------------|:-------------------------------|:-------------------------------------------------|
| 解析类               | BeanDefinitionReader   | AnnotatedBeanDefinitionReader  | ClassPathBeanDefinitionScanner | ConfigurationClassBeanDefinitionReader            |
| 注册的BeanDefinition | GenericBeanDefinition  | AnnotatedGenericBeanDefinition | ScannedGenericBeanDefinition   | ConfigurationClassBeanDefinition（是reader内部类） |
| 注入方式             | xml、properties、groovy | 显示注入，手动注入，内部使用       | @Component及其派生注解           | 使用@Bean注解时候注入                               |



## ApplicationContext
概述：Spring上下文，只读的中央接口；具有IOC、资源管理、国际化、事件监听等功能。  
ConfigurableApplicationContext：扩展了生命周期管理。  
GenericApplicationContext：通用实现，聚合了BeanDefinitionRegistry和DefaultListableBeanFactory.  
AnnotationConfigApplicationContext：默认的非WEB环境的上下文.


## BeanPostProcessor
概述：定义了bean初始化前后的拦截处理。  
初始化前置处理：BeanPostProcessor.postProcessBeforeInitialization  
初始化后置处理：BeanPostProcessor.postProcessAfterInitialization



## InstantiationAwareBeanPostProcessor
概述：BeanPostProcessor的子接口，扩展了bean实例化前后的操作和属性填充后的操作。  
实例化前置操作：InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation  
实例化后置操作：InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation  
属性应用前的处理：InstantiationAwareBeanPostProcessor.postProcessPropertyValues

## DestructionAwareBeanPostProcessor
概述：BeanPostProcessor的子接口，添加了销毁前的回调操作。  
销毁前置操作：DestructionAwareBeanPostProcessor.postProcessBeforeDestruction


## BeanFactoryPostProcessor
概述：BeanFactory的后置处理器，允许修改bean工厂，BeanDefinition；此时bean还未初始化  
postProcessBeanFactory()方法：上下文初始化之后，修改内部的BeanFactory，所有的BeanDefinition都会被加载，可以修改。
### 重要子接口：
InstantiationAwareBeanPostProcessor：扩展了bean实例化前后的回调处理；  
DestructionAwareBeanPostProcessor：扩展了销毁前的回调处理

## BeanWrapper
概述：定义的一些基于Javabean规范的操作，用来操作bean的属性等

## FactoryBean
概述：bean的创建工厂，主要是在框架内部使用。当我们注入FactoryBean对象时，实际注入的是getObject()返回的对象

## ObjectFactory
概述：工厂类接口，实现类延迟加载。在Spring中常以匿名类出现，作为SpringBean三级缓存的元素。


# 二、bean的生命周期
（1）解析、注册BeanDefinition；  
（2）实例化前置操作：可以在实例化前返回bean，从而阻断默认的实例化。  
（3）实例化 BeanWrapper.getWrappedInstance()，**如果存在构造器注入，会在实例化时注入**  
（4）实例化后置操作：提供在属性填充前修改属性值的机会。  
（5）属性填充：自动注入，绑定需要注入的bean，**setter注入和字段注入都在这里实现**。  
（6）初始化前置处理：回调处理Aware接口ApplicationContextAware；PostConstruct初始化处理（基于JSR250初始化）。  
（7）初始化：InitializingBean和InitMethod处理。  
（8）初始化后置处理：目标bean已经初始化结束，可以实现AOP增强，返回代理bean；AbstractAutoProxyCreator。  
（9）销毁前置方法：@PreDestroyestroy  
（10）销毁：bean被销毁，比如resetBeanDefinition，重置BeanDefinition，提供扩展入口。


zongjie:applyBeanPostProcessorsBeforeInitialization

# 三、bean创建流程
概述：流程主要以单例bean、字段注入类型进行说明  
bean创建的核心方法是AbstractAutowireCapableBeanFactory.doCreateBean()。  
singletonObjects：一级缓存，缓存的初始化完成的beanName，bean。  
earlySingletonObjects：二级缓存，缓存的早期引用beanName，bean（只有循环引用才会用到）。  
singletonFactories：三级缓存，缓存的beanName,ObjectFactory（每个bean都会添加，但是添加的是匿名对象，getObject()方法是延迟方法，只有在出现循环依赖时才会调用）。  
singletonsCurrentlyInCreation：对象是否正在创建的标记缓存。  
populateBean()：填充方法。  
initializeBean():初始化方法。



## 3-1 普通bean的加载流程
[创建流程图](./resources/普通bean创建流程（单例、非延迟加载）.png)
1、合并BeanDefinition；
2、实例化；
3、添加到三级缓存；
4、创建bean；
5、填充bean；
6、初始化

## 3-2 AOP增强bean的加载流程
[AOP增强bean创建流程](./resources/AOP增强bean创建流程.png)

1、在执行**初始化后置处理**的时候，会依次调用注册的bean的后置处理器的初始化后置方法"applyBeanPostProcessorsAfterInitialization()"；
2、自动代理创建器会在bean初始化后进行处理，如果当前类需要代理，会创建代理bean替换原生的bean。AbstractAutoProxyCreator.postProcessAfterInitialization()
3、先找到符合当前bean的通知（Advisor）；然后根据以当前类为目标类，当前bean为targetSource去创建代理对象。


## 3-3 AOP增强的、循环依赖bean的创建流程
[创建流程图](./resources/AOP增强的循环依赖bean的创建流程.png)
说明1：出现循环依赖才会调用方法getEarlyBeanReference()，并使用二级缓存earlySingletonObjects；**当前bean需要增强的时候getEarlyBeanReference()返回的是增强的代理bean，否则返回的是原生bean**  
说明2：出现循环依赖时，只有先开始创建的bean才会被添加到二级缓存中。**A-B循环依赖，先创建A，那么只有在填充B时，第二次去创建A的时候，才会将A从三级缓存取出来，存放到二级缓存。**

## 3-4 循环依赖补充
1、使用ObjectFactory的好处：延迟创建
2、为何使用三级缓存：循环依赖本身只需要两层缓存处理，使用三层缓存是因为返回bean的时候，可能需要使用proxy增强，需要加一层缓存去替换首次创建的bean。





