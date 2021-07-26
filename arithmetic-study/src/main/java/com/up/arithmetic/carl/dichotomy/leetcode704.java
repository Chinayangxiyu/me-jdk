package com.up.arithmetic.carl.dichotomy;

/**
 * 给定一个 n 个元素有序的（升序）整型数组 nums 和一个目标值 target  ，
 * 写一个函数搜索 nums 中的 target，如果目标值存在返回下标，否则返回 -1。
 *
 * 二分有两种方案
 * right = nums.length-1 或 right = nums.length；
 * 两种方案会导致left和right判断条件不同
 */
public class leetcode704 {

    /**
     * 方案一：right = nums.length-1
     * left <= right，因为nums[right]元素是生效的
     */
    public int search(int[] nums, int target) {
        int left = 0;
        int right = nums.length-1;
        while (left <= right){
//            int mid = (left + right)/2;
            int mid = left + ((right-left) >> 1);// 等同于(left + right)/2，避免int类型溢出
            if(nums[mid] > target){
                right = mid-1;
            }else if(nums[mid] < target){
                left = mid+1;
            }else {
                return mid;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        leetcode704 l = new leetcode704();
        int[] nums = new int[]{-1,0,3,5,9,12};
        l.search1(nums, 9);
    }


    /**
     * 方案二：right = nums.length
     * 因为nums[right]无效，所以left < right
     */
    public int search1(int[] nums, int target) {
        int left = 0;
        int right = nums.length;
        while (left < right){
            int mid = left + ((right-left) >> 1);// 等同于(left + right)/2，避免int类型溢出
            if(nums[mid] > target){
                right = mid;
            }else if(nums[mid] < target){
                left = mid+1;
            }else {
                return mid;
            }
        }
        return -1;
    }

}
