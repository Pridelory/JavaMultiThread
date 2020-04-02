package cn.itcast.test;

import lombok.extern.slf4j.Slf4j;

import static cn.itcast.n2.util.Sleeper.sleep;

/**
 * 共享变量在多个线程间的可见性
 * 用volatile标识共享变量使其属性变为"易变"
 *
 * java进程内 有一块内存区域即主内存是共享内存，各个线程
 * 内内存是私有，下例的run在共享内存中，但遇到while循环时，
 * JIT编译器会把将 run 的值缓存至自己工作内存中的Cache中，
 * 减少对主存中 run 的访问，提高效率，所以即使在主内存中改了
 * run的值，该线程（t）也不可见。但加上了volatile关键字标识
 * 其为易变时，该线程每次就回去主内存拿run变量了。
 *
 * volatile不能修饰局部变量（线程私有）
 * 它可以用来修饰成员变量和静态成员变量，
 * 他可以避免线程从自己的工作缓存中查找变量的值，
 * 必须到主存中获取 它的值，线程操作 volatile 变量都是直接操作主存
 */
@Slf4j(topic = "c.Test32")
public class Test32 {
    // 易变
    volatile static boolean run = true;

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(()->{
            while(true){
                if(!run) {
                    break;
                }
            }
        });
        t.start();

        log.debug("开始t...");
        sleep(1);
        log.debug("停止t....");
        run = false; // 线程t不会如预想的停下来
    }
}
