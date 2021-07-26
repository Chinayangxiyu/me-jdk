package com.up.arithmetic.carl.dichotomy;

/**
 实现?int sqrt(int x)?函数。
 计算并返回?x?的平方根，其中?x 是非负整数。
 由于返回类型是整数，结果只保留整数的部分，小数部分将被舍去。
 示例 1:
 输入: 4
 输出: 2

 示例 2:
 输入: 8
 输出: 2
 说明: 8 的平方根是 2.82842...,
 ?    由于返回类型是整数，小数部分将被舍去。
 */
public class leetcode69 {

    public int mySqrt(int x) {
        if(x < 1){
            return 0;
        }
        if(x < 4){
            return 1;
        }
        if(x < 9){
            return 3;
        }
        int left = 0;
        int right = x;
        while (left <= right){
            int mid = left + (right-left)/2;
            int sum = mid * mid;
            if(sum > x){
                right = mid -1;
            }else if(sum < x){
                left = mid + 1;
            }else {
                return mid;
            }
        }

        return right;
    }

}
