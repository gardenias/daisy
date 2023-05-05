package jdk.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


class TimestampedCompletableFuture<K> extends CompletableFuture<K> {
    private final long start;
    private boolean success = false;

    public TimestampedCompletableFuture() {
        start = System.currentTimeMillis();
    }

    @Override
    public K get() throws InterruptedException, ExecutionException {
        try {
            K t = super.get();
            success = true;
            return t;
        } finally {
        }
    }

    @Override
    public K get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            K t = super.get(timeout, unit);
            success = true;
            return t;
        } finally {

        }
    }


    public long start() { return start; }

    public boolean isSuccess() { return success; }
}
