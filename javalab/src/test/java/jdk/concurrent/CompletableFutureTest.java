package jdk.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class CompletableFutureTest {


    @Test
    void getTimeOutTest() {
        try {
            AtomicLong start = new AtomicLong(System.nanoTime());
            final CompletableFuture<Long> future = new CompletableFuture<>();
            new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                    start.set(System.nanoTime());
                    System.out.println("future completed " + start);
                    future.complete(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
            System.out.println(System.currentTimeMillis());
            Long value = future.get(1, TimeUnit.MINUTES);
            System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start.get()));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
