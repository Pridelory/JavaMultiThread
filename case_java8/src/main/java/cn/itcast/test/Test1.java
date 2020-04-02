package cn.itcast.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test1")
public class Test1 {

    public static void main(String[] args) {
        Thread t = new Thread(){
            @Override
            public void run() {
                log.debug("running");
            }
        };

        t.start();

        log.debug("running");
    }
    public static void test2() {

        // Runnable任务对象
        // 此种方法更好，因为此时Runnable对象和Thread对象为组合关系
        Thread t = new Thread(()->{ log.debug("running"); }, "t2");

        t.start();
    }
    public static void test1() {
        Thread t = new Thread(){
            @Override
            public void run() {
                log.debug("running");
            }
        };

        t.setName("t1");
        t.start();

    }
}
