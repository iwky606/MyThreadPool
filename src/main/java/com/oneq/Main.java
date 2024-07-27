package com.oneq;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        MyThreadPool myExecutor = new MyThreadPool(5, 10, 1000, new ArrayBlockingQueue<>(5));

        for (int i = 0; i < 12; i++) {
            final int num = i;
            myExecutor.execute(() -> {
                System.out.println("num is: " + num);
            });
        }

        ThreadPoolExecutor executor=new ThreadPoolExecutor(5,10,1000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(5));
        Thread.sleep(1000);
        System.out.println("==============================");
        for (int i = 0; i < 12; i++) {
            final int num = i;
            executor.execute(() -> {
                System.out.println("num is: " + num);
            });
        }
    }
}
