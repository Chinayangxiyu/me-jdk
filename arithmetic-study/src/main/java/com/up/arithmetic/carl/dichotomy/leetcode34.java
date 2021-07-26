package com.up.arithmetic.carl.dichotomy;

import java.util.Arrays;

/**
 34. 在排序数组中查找元素的第一个和最后一个位置
 给定一个按照升序排列的整数数组 nums，和一个目标值 target。找出给定目标值在数组中的开始位置和结束位置。
 如果数组中不存在目标值 target，返回 [-1, -1]。
 进阶：
 你可以设计并实现时间复杂度为 O(log n) 的算法解决此问题吗？

 示例 1：
 输入：nums = [5,7,7,8,8,10], target = 8
 输出：[3,4]
 示例 2：

 输入：nums = [5,7,7,8,8,10], target = 6
 输出：[-1,-1]
 示例 3：

 输入：nums = [], target = 0
 输出：[-1,-1]
 提示：

 0 <= nums.length <= 105
 -109 <= nums[i] <= 109
 nums 是一个非递减数组
 -109 <= target <= 109
 */
public class leetcode34 {


    public static void main(String[] args) {
        leetcode34 l = new leetcode34();
//        int[] nums = new int[]{5,7,7,8,8,10};
        int[] nums = new int[]{0,0,0,1,2,3};
        int[] res = l.searchRange(nums, 0);
        System.out.println(Arrays.toString(res));
    }
    /**
     * 1、先找到任意一个target所在的位置index；如果不存在返回[-1,-1]
     * 2、以index为中点将数组分割为左右两个区间，去左边找小于target的第一个值leftMax，去右边找大于target的第一个值rightMin
     */
    public int[] searchRange(int[] nums, int target) {

        int left = 0;
        int right = nums.length-1;
        while(left <= right){
            int mid = (left + right)/2;
            if(nums[mid] > target){
                right = mid-1;
            }else if(nums[mid] < target){
                left = mid + 1;
            }else{
                int l = mid == 0 ? 0 : findLeftMax(nums, target, mid);
                int r = mid == nums.length-1 ? nums.length-1 : findRightMin(nums, target, mid);
                return new int[]{l, r};
            }
        }

        return new int[]{-1, -1};
    }

    /**
     * 查找mid左边，第一个比mid小的值；mid左边的值 <= target
     */
    private int findLeftMax(int[] nums, int target, int r){

        int left = 0;
        int right = r; //
        while (left <= right){
            int mid = (left + right) / 2;
            // 找到第一个小于target的值
            if(nums[mid] != target && nums[mid+1] == target){
                return mid+1;
                // nums[mid]左边还存在小于target的值，或者mid已经为0
                // 【重要】这一步能保证nums[right+1] == target是必然满足的条件
            }else if(nums[mid] == target){
                right = mid-1;
                // nums[mid]不是第一个小于target的值
            }else{
                left = mid+1;
            }
        }
        // 为什么是left而不是right；因为nums[right+1] == target一直满足；当判断最后一个元素left == right满足时；会有以下三种情况
        // 1、nums[mid] != target；因为nums[right+1] == target为真，且left == right ==mid;所以直接返回mid+1。
        // 2、nums[mid] == target，因为nums[left]左边的元素肯定是小于target的，所以直接返回left。
        // ** 3、nums[mid] < target，因为 left == right == mid 且 nums[right+1] == target所以；
        // 所以nums[left + 1] == target是满足的，如果nums[mid] < target，那么返回mid+1
        return left;
    }

    private int findRightMin(int[] nums, int target, int l){

        int left = l;
        int right = nums.length-1;
        while (left <= right){
            int mid = (left + right) / 2;
            if(nums[mid] > target && nums[mid-1] == target){
                return mid-1;
                // 保证nums[left-1] == target
            }else if(nums[mid] == target){
                left = mid+1;
            }else{
                right = mid-1;
            }
        }

        return right;
    }

}
