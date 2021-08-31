# 1、Introspector(内省)
内省是通过反射实现的，相对于反射提供了更简单的api.
内省操作只针对JavaBean，只有符合JavaBean规则的类的成员才可以采用内省API进行操作.
Javabeans规范；
1、类映射public的；
2、必须有一个空参构造器；
3、属性应该是private的；
4、属性有对应的get/set方法；

5、如果字段为boolean类型, 此时不应该叫个getter方法, 而是is方法, 例如把getName变成isName.
6、如果属性名的第二个字母大写, 那么该属性名直接用作 getter/setter 方法中 get/set 的后部分, 就是说大小写不变. 例如属性名为sName，方法是getsName/setsName.
7、如果前两个字母是大写(专有名词和缩略词等), 这种情况属性名直接接在 getter/setter 方法中 get/set 的后部分. 例如属性名为URL, 方法是getURL/setURL.
8、如果首字母大写，也是属性名直接接在 getter/setter 方法中 get/set 的后部分. 例如属性名为Name, 方法是getName/setName, 这种情况会出问题, 会因为找不到属性而出错: 因为默认的属性名是name.


# 2、SPI（Service Provider Interface）
## 概述：标准服务接口，Java提供了一套用来被第三方实现或者扩展的接口，SPI的作用是从扩展的第三方jar包中去寻找API接口的实现。
## 原理：
（1）第三方在实现jar包的时候，需要在路径"META-INF/services/"添加类全路径名称的文件（文件类型是实现类的全路径名）；  
（2）程序在启动的时候，使用ServiceLoader去扫描依赖jar包的META-INF/services/文件夹；加载对应的class  
（3）ServiceLoader加载工具内部使用"线程上下文类加载器"（contextClassLoader）去加载的class，contextClassLoader
可以通过Thread.setContextClassLoader()设置，未设置会从父线程继承。
## 说明：
**SPI破坏了类加载的双亲委派**，第三方jar包理论上应该有应用程序加载器去加载的，SPI使用contextClassLoader实际可能是启动类
加载器；但是为了实现Java标准服务不得不这么做。
## 栗子
JDBC标准服务使用了SPI机制;  
SpringBoot的模块化扩展机制也是一种SPI思想的，添加新的模块时，在jar包META-INF路径添加spring.factories文件，文件中包含有  
需要被加载的类全限定类名称;
dubbo的SPI。

## SPI和API差异
API是给的接口具体实现，可以直接使用。
SPI是给的接口规范，需要使用方自己实现。



# 3、JDNI(java Naming and Directory Interface):
一种IOC（控制反转）的实现，通过配置将一些bean进行初始化，交由"容器"托管，应用程序不关注这些bean的修改、初始化；  
应用程序根据自己的需要去获取对应的bean即可。比如tomcat中可以部署多个应用程序，这些应用程序使用同一个数据库，就  
可以将数据源配置在tomcat中，各个应用程序就不用配置了，直接使用。这里的"容器"不仅限于tomcat，也可以是Spring也  
可以是其他的容器；JNDI是一种IOC思想。

官网说明：https://www.oracle.com/java/technologies/jndi-overview.html  
概括：Java官方提供的一套命名和目录功能API，可以使用JNDI存储和检索任何类型的命名Java对象。

https://blog.csdn.net/u010430304/article/details/54601302/


# 4、字节码技术
1、ASM
CGLIB底层使用的是ASM
2、javassist
3、Aspectj  
（AOP思想的落地实现，是一门语言，有自己的语法，可以生成class字节码）
https://blog.csdn.net/zhao9tian/article/details/37762389
[百度百科](https://baike.baidu.com/item/Aspectj/4830848?fr=aladdin)

ASM更快、性能更高；javassist更简单


# 5、MethodHandle（方法句柄）
什么是MethodHandle：模拟字节码的方法指令调用；可以实现将方法作为参数进行传递；提供了类似Java反射的"反射"操作。
MethodHandle和Java反射比较：
（1）：MethodHandle是模拟的字节码方法指令调用，Reflection是Java代码层次的调用；MethodHandle的效率更高；  
**更因为MethodHandle是模拟的字节码指令调用，所以它不仅仅适用于Java，而是所有运行在JVM上的语言**；而Reflection只能用于Java。  
（2）：MethodHandle仅仅是方法在Java端的"映像"；包含了签名、描述符等信息比反射更少；是轻量级的。  
（3）：因为MethodHandle是基于字节码的，理论上可以做编译优化（方法内联等）；反射不可以。
```
public class MethodHandleDemo {

    public static void main(String[] args) throws Exception, Throwable {
        Person p = new Person();

        // 1、虚方法方法句柄使用
        MethodType virtualType = MethodType.methodType(int.class, String.class);

        MethodHandle virtualMethod = lookup()
                .findVirtual(p.getClass(), "virtualMethod", virtualType) // 查找虚方法
                .bindTo(p); // 虚方法是实例方法，需要绑定实例；
        virtualMethod.invoke("测试 静态方法句柄调用");

        // invokeExact表示精确调用，不会对参数和返回值执行类型转换；
        // 因为virtualMethod方法的参数是String类型；所以如下调用会报错
//         virtualMethod.invokeExact(11);

        // 2、静态方法，方法句柄使用
        MethodType staticType = MethodType.methodType(void.class);
        MethodHandle statucHandle = lookup().
                findStatic(Person.class, "staticMethod", staticType);// 查找静态方法
        statucHandle.invokeExact();

    }
}


class Person{


    public int virtualMethod(String n){
        System.out.println("this is a virtual method；param=" + n );
        return 100;
    }

    public static final void staticMethod(){
        System.out.println("this a static method");
    }
}
```


# 6、虚方法、非虚方法(涉及方法分派)
类加载能直接将方法的符号引用替换为直接引用的方法称为"非虚方法"，包括static,private,super,final修饰的方法和构造器方法。  
其他方法为虚方法。
