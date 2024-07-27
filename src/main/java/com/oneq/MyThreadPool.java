package com.oneq;

import java.nio.channels.Pipe;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPool {

    private ConcurrentSkipListSet<WorkThread> threads;
    private ReentrantLock lock;
    private int coreThread;
    private int maxThread;
    private long timeOut;
    private BlockingQueue<Runnable> queue;
    private static AtomicLong id = new AtomicLong(1);

    public MyThreadPool(int coreThread, int maxThread, long timeOut, BlockingQueue<Runnable> queue) {
        this.coreThread = coreThread;
        this.maxThread = maxThread;
        this.timeOut = timeOut;
        this.queue = queue;
        lock = new ReentrantLock();
        threads = new ConcurrentSkipListSet<>();
    }

    public int getThreadNum() {
        return threads.size();
    }

    public void execute(Runnable task) {
        try {
            lock.lock();
            if (threads.size() < coreThread) {
                addThread(task);
                return;
            }
        } finally {
            lock.unlock();
        }

        try {
            queue.add(task);
        } catch (IllegalStateException e) {
            // 超过数量
            try {
                lock.lock();
                if (threads.size() < maxThread) {
                    addThread(task);
                } else {
                    // 默认抛出异常
                    throw new IllegalStateException();
                }
            } finally {
                lock.unlock();
            }
        }

    }

    private void addThread(Runnable task) {
        WorkThread workThread = new WorkThread(task, id.incrementAndGet());
        workThread.start();
        threads.add(workThread);
    }


    class WorkThread extends Thread implements Comparable<WorkThread> {
        private Runnable task;
        private long id;

        public WorkThread(Runnable task, long id) {
            this.task = task;
            this.id = id;
        }

        @Override
        public void run() {
            while (true) {
                if (task != null) {
                    task.run();
                    task = null;
                }
                try {
                    Runnable task = queue.poll(timeOut, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        task.run();
                    } else {
                        try {
                            lock.lock();
                            if (threads.size() > coreThread) {
                                threads.remove(this);
                                return;
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public int compareTo(WorkThread o) {
            return Long.compare(id, o.id);
        }
    }

}