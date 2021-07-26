5、SpringIOC依赖查找
延迟查找的含义；
BeanFactory具有双亲委派机制

6、IOC核心类
6-1、  
BeanDefinition：定义bean的元数据信息，包含作用域，对应bean类型信息，lazy，primary等信息（parent属性使其具有层次结构）；
核心实现GenericBeanDefinition：

BeanDefinitionReader：读取BeanDefinition的接口，比如从xml文件中读取BeanDefinition使用XmlBeanDefinitionReader；但
**并不要求所有的BeanDefinition的来源都是由BeanDefinitionReader提供**。

6-2、
BeanDefinitionRegistry接口：BeanDefinition注册中心抽象接口；bean是根据BeanDefinition创建的；如果需要创建bean就必须对
BeanDefinition进行管理。


6-3、BeanFactory：bean容器的根接口，提供了getBean、getType等基于bean容器的基本操作；
重要子接口
ListableBeanFactory：扩展BeanFactory，根据类型获取bean（list）。
HierarchicalBeanFactory：扩展BeanFactory具有层次结构，具有parentBeanFactory。

6-4、  
ApplicationContext接口：Spring应用程序的核心接口，不仅仅是IOC（IOC只是其继承自BeanFactory的功能），还整合资源管理  
（ResourceLoader），国际化（MessageSource），事件发布（ApplicationEventPublisher）功能。





bean添加到IOC容器的步骤
1、解析BeanDefinition信息，并注册到DefaultListableBeanFactory.beanDefinitionMap；
2、根据  InstantiationStrategy.instantiate创建实例bean
3、创建实例的过程中需要装配依赖。



##  6-5、自动装配（绑定）
BeanPostProcessor：在bean初始化前，初始化后做一些操作,实现类似拦截器的功能
BeanPostProcessor.postProcessBeforeInitialization()拦截bean初始化之前。
BeanPostProcessor.postProcessAfterInitialization()拦截bean初始化之后


AutowiredAnnotationBeanPostProcessor：处理@AutoWired、@Value(装配属性)、@Inject的自动装配；前两个是Spring自己的；
@Inject是JSR330提出的。

CommonAnnotationBeanPostProcessor：主要支持JSR250提出的注解，包括@Resource,@PostConstruct,@PreDestory;和  
AutowiredAnnotationBeanPostProcessor相比除了装配bean，还实现了bean的init和destroy操作。
自动装配的步骤：  
（1）、反射获取当前bean的添加了注解（@AutoWired、@Resource)的字段和方法；构建自动装配需要的元信息（InjectionMetadata）。
（2）、查找需要装配的bean：InjectionMetadata.inject()方法中根据步骤1"元信息"，去beanFactory中查找对应的bean。
（3）、执行绑定：调用反射方法，将查到的bean和当前对象绑定。
（4）\ Autowired annotation is not supported on static fields

InstantiationAwareBeanPostProcessor接口的postProcessProperties()方法是将bean绑定对应的属性之前做的处理；
AutowiredAnnotationBeanPostProcessor和CommonAnnotationBeanPostProcessor都实现了该接口；作用就是执行我们上述的自动装配。


## 6-6、refresh（加载，启用上下文）
ConfigurableApplicationContext.refresh():加载Spring上下文


## 6-6、
ConfigurableListableBeanFactory.registerResolvableDependency方法会注册一下特殊bean，比如BeanFactory.class、ApplicationContext.class
等。这些bean不能通过依赖查找（getBean） 找到，因为依赖查找使用的是ApplicationContext内部的BeanFactory；但是这些bean没有注册到beanFactory；最终
是缓存到了DefaultListableBeanFactory.resolvableDependencies属性中，并在依赖注入（绑定）的时候进行特殊处理。
处理代码如下：
```
DefaultListableBeanFactory.findAutowireCandidates();
```


问题总结：IOC里面注册的bean是怎么来的；
（1）BanDefinition由来：  
AnnotatedBeanDefinitionReader：解析注解，如@Bean
ClassPathBeanDefinitionScanner：扫描classPath
最终都是调用DefaultListableBeanFactory.registerBeanDefinition()注册BeanDefinition。

bean的由来：DefaultListableBeanFactory.getBean();

BeanPostProcessor：bean的处理器
BeanFactoryPostProcessor：beanFactory钩子，允许自定义修改应用程序上下文的bean定义，调整上下文底层bean工厂的bean属性值。BeanDefinition
已经加载，但是bean还未实例话，去修改BeanDefinition的属性值。



AutowireCapableBeanFactory:具有自动装配能力的BeanFactory
SingletonBeanRegistry:注册一些公用的单例bean，这些bean；比如