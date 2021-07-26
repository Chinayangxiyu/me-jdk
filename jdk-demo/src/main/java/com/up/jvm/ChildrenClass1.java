package com.up.jvm;

/**
 * @author: yangxiyu
 * @date: 2021/7/2 10:51
 * @version: 1.0
 * @see
 */
public class ChildrenClass1 extends ParentClass {


    @Override
    public void method1(){
        System.out.println("ChildrenClass1  method1");
    }

    public static void main(String[] args) {
        ChildrenClass1 c1 = new ChildrenClass1();
        c1.method1();

        ParentClass c2 = new ChildrenClass1();
        c2.method1();

    }
}
