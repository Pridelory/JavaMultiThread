package cn.itcast.test;

import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;

import java.util.Vector;
import java.util.concurrent.locks.LockSupport;

/**
 * 偏向锁测试
 */
@Slf4j(topic = "c.TestBiased")
public class TestBiased {

    static Thread t1,t2,t3;

    public static void main(String[] args) throws InterruptedException {
//        test1();
//        test4();
//        test2();
        test3();
    }


    /**
     * 测试观察 偏向锁 在初始、加锁、解锁的状态
     */
    public static void test1() {
        Dog dog = new Dog();
        log.debug(ClassLayout.parseInstance(dog).toPrintable());

        synchronized (dog) {
            log.debug(ClassLayout.parseInstance(dog).toPrintable());
        }

        log.debug(ClassLayout.parseInstance(dog).toPrintable());

        // 偏向锁前调用hashCode会使得其默认的可偏向状态（101）变为不可偏向的正常状态（001）
        // 因为偏向状态对象头对应格式没地儿存储哈希码
//        dog.hashCode();
        log.debug(ClassLayout.parseInstance(dog).toPrintable());
    }

    /**
     * 偏向锁撤销 -- 其他线程使用对象
     * 此时偏向锁会升级为轻量级锁
     *
     */
    public static void test2() throws InterruptedException{
        Dog d = new Dog();
        Thread t1 = new Thread(() -> {
            // 线程一给d对象加锁前 markword后几位为101，表示java对象默认为偏向锁
            log.debug(ClassLayout.parseInstance(d).toPrintable());
            synchronized (d) {
                // 线程一给d对象加锁后，此时还是偏向锁，只不过偏向锁前的字段被设置为线程一的线程ID
                log.debug(ClassLayout.parseInstance(d).toPrintable());
            }
            // 解锁后对象d的所状态不变，线程id也不变（留在里面了）
            log.debug(ClassLayout.parseInstance(d).toPrintable());
            synchronized (TestBiased.class) {
                TestBiased.class.notify();
            }
        }, "t1");
        t1.start();


        Thread t2 = new Thread(() -> {
            synchronized (TestBiased.class) {
                try {
                    TestBiased.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 在线程二给d对象加锁前，该对象保持上面最后的状态
            log.debug(ClassLayout.parseInstance(d).toPrintable());
            synchronized (d) {
                // 线程二（作用于该对象的另一个线程）给d对象加锁后，出现了锁撤销的情况
                // java对象头锁字段变为了00 说明其由偏向锁升级为轻量级锁
                log.debug(ClassLayout.parseInstance(d).toPrintable());
            }
            // 线程二给d对象撤销锁后，改对象变为normal对象，即无锁状态
            log.debug(ClassLayout.parseInstance(d).toPrintable());
        }, "t2");
        t2.start();
    }

    /**
     * 批量重偏向
     *
     * 当撤销偏向锁（把偏向t1的锁升级为轻量级锁）阀值超过20时，
     * jvm就会直接把线程t2的线程id直接设置到java对象头中，
     * 这样就不会导致升级为轻量级锁而是偏向t2的锁了
     */
    public static void test3() throws InterruptedException{
        Vector<Dog> list = new Vector<>();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 30; i++) {
                Dog d = new Dog();
                list.add(d);
                synchronized (d) {
                    // 给30个d对象加偏向锁，这30个对象头中都被设置了线程1的线程ID
                    log.debug(ClassLayout.parseInstance(d).toPrintable());
                }
            }

            synchronized (list) {
                list.notify();
            }
        }, "t1");
        t1.start();

        Thread t2 = new Thread(() -> {
            try {
                synchronized (list) {
                    list.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.debug("============================================================================================================");
            for (int i = 0; i < 30; i++) {
                Dog d = list.get(i);
                log.debug(ClassLayout.parseInstance(d).toPrintable());
                synchronized (d) {
                    // 前20个循环发生了什么？
                    // 线程二的一直干预导致偏向线程一的锁被撤销了二十次（升级为了轻量级锁）
                    // 达到阀值后（20次），jvm这时候就想是不是偏向错了，
                    // 于是把剩下的十个对象的锁偏向线程二，即剩下的十个对象头都被设置了线程2的线程ID
                    // 即剩下的是个对象还是偏向锁，没有发生锁升级成轻量级锁
                    log.debug(ClassLayout.parseInstance(d).toPrintable());
                }
                log.debug(ClassLayout.parseInstance(d).toPrintable());
            }

        }, "t2");
        t2.start();

    }

    /**
     * 批量撤销
     *
     * 当撤销批量锁阀值超过40次后，jvm会这么觉得：确实偏向错了，本就不应该偏向，
     * 于是这个对象所属的类就变为不可偏向的，随后新建的对象也变为不可偏向的
     * @throws InterruptedException
     */
    private static void test4() throws InterruptedException {
        Vector<Dog> list = new Vector<>();

        int loopNumber = 39;
        t1 = new Thread(() -> {
            for (int i = 0; i < loopNumber; i++) {
                Dog d = new Dog();
                list.add(d);
                synchronized (d) {
                    log.debug(i + "\t" + ClassLayout.parseInstance(d).toPrintable(true));
                }
            }
            LockSupport.unpark(t2);
        }, "t1");
        t1.start();

        t2 = new Thread(() -> {
            LockSupport.park();
            log.debug("===============> ");
            for (int i = 0; i < loopNumber; i++) {
                Dog d = list.get(i);
                log.debug(i + "\t" + ClassLayout.parseInstance(d).toPrintable(true));
                synchronized (d) {
                    log.debug(i + "\t" + ClassLayout.parseInstance(d).toPrintable(true));
                }
                log.debug(i + "\t" + ClassLayout.parseInstance(d).toPrintable(true));
            }
            LockSupport.unpark(t3);
        }, "t2");
        t2.start();

        t3 = new Thread(() -> {
            LockSupport.park();
            log.debug("===============> ");
            for (int i = 0; i < loopNumber; i++) {
                Dog d = list.get(i);
                log.debug(i + "\t" + ClassLayout.parseInstance(d).toPrintable(true));
                synchronized (d) {
                    log.debug(i + "\t" + ClassLayout.parseInstance(d).toPrintable(true));
                }
                log.debug(i + "\t" + ClassLayout.parseInstance(d).toPrintable(true));
            }
        }, "t3");
        t3.start();

        t3.join();
        log.debug(ClassLayout.parseInstance(new Dog()).toPrintable(true));
    }
}

class Dog {

}
