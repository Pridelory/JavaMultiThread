package cn.itcast.test;

import cn.itcast.Constants;
import cn.itcast.n2.util.FileReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test4")
public class Test4 {

    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.debug("running...");
                FileReader.read(Constants.MP4_FULL_PATH);
            }
        };

        // 直接调用t1.run() 不会开辟异步的新线程
        // 这种形式其实还是主线程去调用run()方法
        // 所以必须用t1.start();
//        t1.run();

        t1.start();
        log.debug("do other things...");
    }
}
