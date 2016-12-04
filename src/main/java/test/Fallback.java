package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.reactivex.Observable.just;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.observeSyncTestOperation;
import static test.TestUtil.output;
import static test.TestUtil.startTest;

public class Fallback {
    public static void main(final String[] args) {
        new Fallback().run();
    }

    public void run() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("fallback on timeout");
        observeSyncTestOperation(1, "")
            .subscribeOn(scheduler)
            .timeout(1000, TimeUnit.MILLISECONDS)
            .onErrorReturn(exception -> {
                if (exception instanceof TimeoutException) {
                    return "default-value";
                } else {
                    throw new RuntimeException(exception);
                }
            })
            .onExceptionResumeNext(just("ooops"))
            .blockingSubscribe(s -> output("                                " + s));

        final Observable<String> syncOperation1 =
            observeSyncTestOperation(1, "")
                .subscribeOn(scheduler);
        final Observable<String> syncOperation2 =
            observeSyncTestOperation(2, "")
                .subscribeOn(scheduler);

        startTest("simple fallback a");
        if (true) {
            final String s =
                syncOperation1
                    .concatWith(syncOperation2)
                    .firstElement()    // o2 should never subscribe
                    .subscribeOn(scheduler)
                    .blockingGet();
            output("                                " + s);
        }

        startTest("simple fallback b");
        if (true) {
            final String s =
                Observable.<String>empty()
                    .concatWith(syncOperation2)
                    .firstElement()    // o2 should subscribe this time
                    .subscribeOn(scheduler)
                    .blockingGet();
            output("                                " + s);
        }
    }
}
