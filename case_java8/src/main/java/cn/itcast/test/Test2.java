package cn.itcast.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j(topic = "c.Test2")
public class Test2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况
        FutureTask<Integer> task = new FutureTask<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("running....");
                Thread.sleep(2000);
                return 100;
            }
        });

        new Thread(task, "t1").start();

        log.debug("{}", task.get());
    }
}
