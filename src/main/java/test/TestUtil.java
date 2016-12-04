package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

import static io.reactivex.Observable.defer;
import static io.reactivex.Observable.empty;
import static io.reactivex.Observable.just;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class TestUtil {
    public static final String DELIBERATE_EXCEPTION = "Deliberate exception";
    private static volatile long startTime = System.currentTimeMillis();

    public static void startTiming() {
        startTime = System.currentTimeMillis();
    }

    public static Observable<Integer> stream(final int maxCount) {
        return stream(maxCount, i -> i);
    }

    public static Observable<String> stream(final int maxCount, final String prefix) {
        return stream(maxCount, i -> prefix + " " + i);
    }

    public static <T> Observable<T> stream(final int maxCount, final Function<Integer, T> mapper) {
        return stream(maxCount, -1, mapper, 100);
    }

    public static <T> Observable<T> stream(
        final int maxCount,
        final int errorAfter,
        final Function<Integer, T> mapper,
        final int periodMsec
    ) {
        return stream(maxCount, errorAfter, mapper, periodMsec, true);
    }

    public static <T> Observable<T> stream(
        final int maxCount,
        final int errorAfter,
        final Function<? super Integer, ? extends T> mapper,
        final int periodMsec,
        final boolean shouldComplete
    ) {
        return Observable.generate(
            () -> 0,
            (i, emitter) -> {
                pause((long) (Math.random() * periodMsec));

                if (i == maxCount) {
                    emitter.onComplete();
                } else if (i == errorAfter) {
                    output("%s %s", i, DELIBERATE_EXCEPTION);
                    emitter.onError(new RuntimeException(DELIBERATE_EXCEPTION));
                } else {
                    final T v = mapper.apply(i);
                    output("%s -> %s", i, v);
                    emitter.onNext(v);
                }
                return i + 1;
            }
        );
    }

    public static boolean pause(final long millis) {
        final long startMs = System.currentTimeMillis();
        try {
            // NOTE: Only using Thread.sleep here as an artificial demo.
            Thread.sleep(millis);
            return true;
        } catch (final InterruptedException e) {
            output("pause interrupted after %s of %s msec", System.currentTimeMillis() - startMs, millis);
            return false;
        }
    }

    public static void output(final String format, final Object... args) {
        System.out.printf("%3d %4dms %s%n", Thread.currentThread().getId(), getElapsedMsec(), String.format(format, args));
    }

    public static long getElapsedMsec() {
        return System.currentTimeMillis() - startTime;
    }

    public static Observable<String> observeSyncTestOperation(final Integer input, final String prefix) {
        return observeSynchronous(i -> syncTestOperation(i, prefix), input);
    }

    public static String syncTestOperation(final Integer i, final String prefix) {
        return syncTestOperation(i, prefix, 2000);
    }

    public static String syncTestOperation(final Integer i, final String prefix, final int msec) {
        final long randomDurationMsec = (long) (Math.random() * msec);
        output("                %s%s started with duration %s msec", prefix, i, randomDurationMsec);

        if (pause(randomDurationMsec)) {
            output("                %s%s done", prefix, i);
            return "result:" + i * 100;
        } else {
            return null;
        }
    }

    // the anSWER...
    //   Super when Writing to (passing into) the object, ie pass into the function
    //   Extends when Reading from the object, read from the function
    public static <T, R> Observable<R> observeSynchronous(final Function<? super T, ? extends R> operation, final T input) {
        return defer(
            () -> {
                final R value = operation.apply(input);
                return value == null ? empty() : just(value);
            }
        );
    }

    public static void startTest(final String name) {
        startSection(name);
        startTiming();
    }

    public static void startSection(final String name) {
        System.out.println("---------------------------------------------------");
        System.out.println(name);
        System.out.println("---------------------------------------------------");
    }

    public static Scheduler schedulerA = Schedulers.from(newFixedThreadPool(10, new CustomisingThreadFactory("Sched-A-%d")));
    public static Scheduler schedulerB = Schedulers.from(newFixedThreadPool(10, new CustomisingThreadFactory("Sched-B-%d")));
    public static Scheduler schedulerC = Schedulers.from(newFixedThreadPool(10, new CustomisingThreadFactory("Sched-C-%d")));

    static class CustomisingThreadFactory implements ThreadFactory {
        public CustomisingThreadFactory(final String pattern) {
            name = String.format(pattern, Thread.currentThread().getId());
        }

        @Override
        public Thread newThread(final Runnable r) {

            final Thread thread = new Thread(r, name);
            thread.setDaemon(true);

            return thread;
        }

        private final String name;
    }
}

