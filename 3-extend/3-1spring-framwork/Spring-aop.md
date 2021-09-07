# 一、AOP
## 1.1、AspectJ和Spring AOP区别
AspectJ 用于编织切面的一门编程语言
[aspectj文档](https://www.eclipse.org/aspectj/doc/released/devguide/)
Spring AOP是基于代理（jdk,CGLIB）的，CGLIB是字节码底层技术生成一个代理类（区别AspectJ，AspectJ是直接修改源class）；

Spring AOP：仅仅支持Spring bean的织入；适用Spring。
AspectJ：是支持任何类，更大，功能更全，Spring接入了对 AspectJ的支持。

# 二、AOP基础
## 2.1、AOP概念：面向切面编程，切面、连接点、切入点是核心概念。
切面：可以是方法、属性。  
连接点：程序执行的点，比如方法执行前、异常处理、方法执行后。  
切入点：匹配连接点的说明。

## 2.2、Spring AOP
描述：SpringAOP是Java实现的，基于IOC容器注册的bean实现的切面编程，目前仅支持方法执行的连接点。Spring AOP虽然没有提供全面的AOP解决方案  
但是能解决我们的大部分问题，当我们需要完整的AOP框架时，可以接入Aspectj。
AOP代理：默认使用基于接口的JDK，基于类继承的使用CGLIB。
DefaultAopProxyFactory.createAopProxy()方法会根据proxyTargetClass状态、目标类的类型去区分选择哪种代理。

Spring AOP使用AspectJ 5声明的注解、切入点解析和匹配。

## 2.3、Cglib增强
ConfigurationClassPostProcessor:如果一个类是@Configuration配置类，会进行增强。
ConfigurationClassPostProcessor.postProcessBeanFactory():使用cglib增强的子类替换目标类；

# 三、事务
## 3.1 事务原理

## 3.2、事务失效原因分析
[引用](https://www.jianshu.com/p/4120b89190d0)
1、bean没有被Spring管理：  
&emsp;&emsp;  原因分析：事务是基于Spring AOP的，Spring AOP仅适用于Spring bean;  
2、方法不是public修饰：  
&emsp;&emsp;  原因分析：事务是基于Spring AOP实现的，Spring AOP是基于JDK和CGLIB动态代理实现的  
JDK动态代理是基于接口的，接口的方法修饰符肯定是public的。  
CGLIB动态代理是通过继承目标类，重写方法，生成代理类。private方法不能被继承；final方法不能被重写；最终  
在执行代理方法的时候，不一定在同一个包下面，所以修饰符也不能是default，更不会是protected。只能是public。

3、自身调用：
```
public class A{

    // 分析(1)、method1没加事务，外部调用method1()的时候调用的是A类本身实例；
    // method1中调用method2，默认使用的是this；Spring事务是基于AOP的，有事务注解的方法
    // 对生成对应的增强代理类proxy，method1中的this调用的是A的实例方法，不是proxy；所以事务失效
    public void method1(){
        this.method2();
    }
    
    @Transactional
    public void method2(){
    }
    
    // 同分析(1)，method3中调用的method4也不是增强代理类，所以事务失效。
    // 如果正常，method4是一个新事务，回滚操作不会影响method3；但是实际影响到了method3()所以失效。
    // 如果使用的是默认的事务传播机制，那么此刻method4是否加事务都能生效，因为整个执行逻辑都包含在了
    // method3声明的事务里面。
    @Transactional
    public void method3(){
        this.method4();
    }
    
    @Transactional( propagation = Propagation.REQUIRES_NEW)
    public void method4(){
    }
}
```
【注意】：Spring事务默认的传播机制是有事务加入，没有事务新建；所以平时在使用的时候类似方法method4()的调用
可以不指定事务传播机制，调用method4()如果
4、方法是final修饰的：final修饰的方法不能被重写，而当前方法如果是final修饰的，肯定方法已经实现了；AOP肯定是基于CGLIB完成的，
CGLIB代理增强类是去继承目标类，重写方法。
