package cn.itcast.test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * 原子操作
 */
public class Test34 {
    public static void main(String[] args) {

        AtomicInteger i = new AtomicInteger(5);

//        System.out.println(i.compareAndSet(5,6)); // 一般要配合while操作

        // 以下两个方法都可以保证各自的两个操作都是一个原子的整体
        /*System.out.println(i.incrementAndGet()); // ++i   1
        System.out.println(i.getAndIncrement()); // i++   2

        System.out.println(i.getAndAdd(5)); // 2 , 7
        System.out.println(i.addAndGet(5)); // 12, 12*/

        //             读取到    设置值
//        i.updateAndGet(value -> value * 10);

//        System.out.println(updateAndGet(i, p -> p / 2));

//        i.getAndUpdate()
        System.out.println(i.get());
    }

    /**
     * 自己实现updateAndGet
     * @param i
     * @param operator
     * @return
     */
    public static int updateAndGet(AtomicInteger i, IntUnaryOperator operator) {
        while (true) {
            int prev = i.get();
            int next = operator.applyAsInt(prev);
            if (i.compareAndSet(prev, next)) {
                return next;
            }
        }
    }
}
