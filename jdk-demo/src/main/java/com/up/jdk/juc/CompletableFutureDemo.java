package com.up.jdk.juc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

/**
 * TODO
 * Description
 *
 * @author xiyu
 * @date 2021-03-22 16:43
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws Exception{

//        CompletableFutureDemo d = new CompletableFutureDemo();
////        d.pipeline();


        LockSupport.parkNanos(CompletableFutureDemo.class, 1000 * 1000 * 1000 * 10L);

        System.out.println("_--------*******");



    }

    private void pipelineTest(){
        CompletableFuture.supplyAsync(() -> {
            System.out.println("-------supplyAsync");
            return 100;
        }).whenComplete((r, t) ->{


        });
    }

    private void async(){
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return " execute supply";
        });

        future.thenApply((param)->{

            try {
                Thread.sleep(5000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("thenApply" + param);
            return "aa:" + param;
        });


        System.out.println("main is ending");
    }

    private void pipeline(){

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return " execute supply";
        });

        future.thenApply((result) ->{
            System.out.println(result + " thenApply");
            return result + " thenApply";
        })
                .thenAccept((result) -> {
                    System.out.println(result + " thenAccept");
                })
                .thenRun(() -> {
                    System.out.println("thenRun");

                });

        System.out.println("pipeline ending");
    }
}


