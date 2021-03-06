package cn.itcast.test;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 无锁AtomicInteger的应用
 *
 * 基于Unsafe
 */
public class TestAccount {
    public static void main(String[] args) {

        // 特别快（无锁）
        Account account = new AccountCas(10000);
        Account.demo(account);

        // 特别慢（有锁）
        AccountUnsafe accountUnsafe = new AccountUnsafe(10000);
        Account.demo(accountUnsafe);
    }
}

/**
 * 无锁
 */
class AccountCas implements Account {
    private AtomicInteger balance;

    public AccountCas(int balance) {
        this.balance = new AtomicInteger(balance);
    }

    @Override
    public Integer getBalance() {
        return balance.get();
    }

    @Override
    public void withdraw(Integer amount) {
        // 注重分析一下过程
//        while(true) {
//            // 获取余额的最新值
//            int prev = balance.get();
//            // 要修改的余额
//            int next = prev - amount;
//            // 真正修改
//            // compareAndSet即CAS 是原子操作
//            // 最重要的是AtomicInteger封装的Unsafe的"万能方法"
//            if(balance.compareAndSet(prev, next)) {
//                break;
//            }
//        }
        // 原子操作
        balance.getAndAdd(-1 * amount);
    }
}

/**
 * 有锁
 */
class AccountUnsafe implements Account {

    private Integer balance;

    public AccountUnsafe(Integer balance) {
        this.balance = balance;
    }

    @Override
    public Integer getBalance() {
        synchronized (this) {
            return this.balance;
        }
    }

    @Override
    public void withdraw(Integer amount) {
        synchronized (this) {
            this.balance -= amount;
        }
    }
}

interface Account {
    // 获取余额
    Integer getBalance();

    // 取款
    void withdraw(Integer amount);

    /**
     * 方法内会启动 1000 个线程，每个线程做 -10 元 的操作
     * 如果初始余额为 10000 那么正确的结果应当是 0
     */
    static void demo(Account account) {
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ts.add(new Thread(() -> {
                account.withdraw(10);
            }));
        }
        long start = System.nanoTime();
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println(account.getBalance()
                + " cost: " + (end-start)/1000_000 + " ms");
    }
}
