package cn.itcast.test;

import java.util.ArrayList;

public class TestForeach {
    public static void main(String[] args) {
        ArrayList<Integer> integers = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            integers.add(i);
        }

        // forEach遍历
        integers.forEach((i) -> System.out.println("输出" + i));
    }
}
