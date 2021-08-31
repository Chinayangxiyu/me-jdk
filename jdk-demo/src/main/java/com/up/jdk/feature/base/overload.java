package com.up.jdk.feature.base;

import java.io.Serializable;

/**
 * @author: yangxiyu
 * @date: 2021/8/12 10:24 上午
 * @version: 1.0
 * @see
 */
public class overload {

    /**
     * 'a'类型是char，转为包装类型Character，Character先实现了Serializable再继承Object
     * 所以根据继承关系优先选择参数为Serializable的重载方法。
     * Character同时实现了Comparable和Serializable，参数类型为Comparable的方法编译无法通过
     *
     * @param args
     */
    public static void main(String[] args) {
        overload o = new overload();
        o.get('a');
    }
    public void get(Object obj) {
        System.out.println("obj");
    }

    public void get(Serializable obj) {
        System.out.println("Serializable");
    }

//    public void get(Comparable obj) {
//        System.out.println("obj");
//    }
}
