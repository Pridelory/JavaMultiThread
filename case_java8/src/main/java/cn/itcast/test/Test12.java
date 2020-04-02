package cn.itcast.test;

import lombok.extern.slf4j.Slf4j;

/**
 * 打断
 * 注意区分interrupt()和isInterrupt()
 * 前者会清除打断标记而后者不会清除
 * 目前我认为 打断标记默认为false 表明没打断
 * 清除了 就为true 意为打断了
 * 没清除 就为false 意为没打断
 *
 */
@Slf4j(topic = "c.Test12")
public class Test12 {

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            while(true) {
                boolean interrupted = Thread.currentThread().isInterrupted();
                if(interrupted) {
                    log.debug("被打断了, 退出循环");
                    break;
                }
            }
        }, "t1");
        t1.start();

        Thread.sleep(1000);
        log.debug("interrupt");
        t1.interrupt();
    }
}
