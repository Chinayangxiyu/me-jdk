package com.up.xianliu;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于环链实现滑动窗口算法【时间轮】
 */
public class SlidingWindow {

    /**
     * 当前时间对应的节点
     */
    private Node curNode;

    /**
     * 滑动窗口的时间周期
     */
    private long seconds;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * 当前窗口时间内累计的请求数量
     */
    private  AtomicInteger total;
    /**
     * 当前时间窗口允许的最大请求数量
     */
    private  int maxTotal;

    // 每个刻度停顿时间
    private long timeOut;

    public SlidingWindow(long seconds, int maxTotal, int total) {
        this.seconds = seconds;
        this.maxTotal = maxTotal;
        this.timeOut = seconds * 1000000;
        this.total = new AtomicInteger(0);
    }

    public static void main(String[] args) throws Exception{
        // 每200毫秒一个刻度
        SlidingWindow s = new SlidingWindow(200, 5, 0);


        // 打开限流
        Thread open = new Thread(()->{
            try {
                s.init();
            }catch (Exception e){
                e.printStackTrace();
            }

        });
        open.start();

        // 等待限流开启
        Thread.sleep(100);

        for(int i = 0; i < 10; i++){

            // 模拟请求线程
            try {
                Thread.sleep(20L);
            }catch (Exception e){
                e.printStackTrace();
            }

            Thread t = new Thread(() -> {

                boolean b = s.getSign();
                System.out.println(b + ":"+Thread.currentThread().getName());
            }, "thread" + i);
            t.start();
        }

        Thread.sleep(1000000L);
    }


    public void init(){
        Node first = new Node();
        first.setCount(new AtomicInteger(0));
        Node prev = first;

        // 计算环链的节点，
        int size = 1000/(int)seconds;
        for(int i = 1;i < size; i++){
            Node n = new Node();
            n.setCount(new AtomicInteger(0));
            prev.setNext(n);
            prev = n;
        }

        prev.setNext(first);

        Node cur = first;
        while (cur != null){
            curNode = cur;
            // 当前线程阻塞时间(当前刻度暂停时间)
            LockSupport.parkNanos(timeOut);

            Node next = cur.getNext();
            int oldCount = next.getCount().get();

            System.out.println("total pre:" + total.get());
            // 总的累计流量减去失效的第一个刻度的流量
            while (!total.compareAndSet(total.get(), total.get() - oldCount)){

            }

            System.out.println("total after:" + total.get());

            // 最老的刻度清零
            next.getCount().set(0);
            // 复用
            cur = next;

        }
    }


    public  boolean getSign(){

        if(total.get() >= maxTotal){
            System.out.println("OutOf capacty  " + Thread.currentThread().getName());
            return false;
        }

        lock.lock();
        try {

            // 当前节点需要加一个请求
            curNode.getCount().incrementAndGet();
            // 总的限流数量加1
            total.incrementAndGet();
        }finally {
            lock.unlock();
        }

        return true;

    }

}




class Node{

    /**
     * 下一个节点
     */
    private Node next;

    /**
     * 当前节点请求的记录数量
     */
    private AtomicInteger count;

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public AtomicInteger getCount() {
        return count;
    }

    public void setCount(AtomicInteger count) {
        this.count = count;
    }
}
