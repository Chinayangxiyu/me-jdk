package com.up.jdk.feature.base;

import java.util.List;

/**
 * @author yxy
 * @date 2020/4/22 11:57
 * @description
 */
public class Student extends Person {

    private List<Double> scores;


    public Student(String name, int age, int gender) {
        super(name, age, gender);
    }

    public Student(String name, int age, int gender, List<Double> scores) {
        super(name, age, gender);
        this.scores = scores;
    }

    public List<Double> getScores() {
        return scores;
    }

    public Student setScores(List<Double> scores) {
        this.scores = scores;
        return this;
    }
}
