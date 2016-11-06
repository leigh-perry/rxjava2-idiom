package test;

import io.reactivex.Scheduler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static io.reactivex.Observable.just;
import static io.reactivex.Observable.timer;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.output;
import static test.TestUtil.startTest;

public class DelayVsTimer {
    public static void main(final String[] args) {
        new DelayVsTimer().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("timer delay first");
        timer(1, SECONDS)
            .flatMap(i -> just(1, 2, 3))
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output(s.toString()));

        startTest("delay first");
        just(1, 2, 3)
            .delay(1, SECONDS)
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output(s.toString()));

        startTest("delay each");
        just(1, 2, 3)
            .delay(i -> timer(i * 500, MILLISECONDS))
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output(s.toString()));
    }
}
