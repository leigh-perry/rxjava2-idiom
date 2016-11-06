package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

import static io.reactivex.Observable.just;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.output;
import static test.TestUtil.startTest;

public class Distinct {
    public static void main(final String[] args) {
        new Distinct().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        final Observable<Integer> observable = just(1, 2, 3, 2, 2, 2, 4, 2, 5);

        startTest("not distinct");
        observable
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("  " + s));

        startTest("distinct");
        observable
            .distinct()
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("  " + s));

        startTest("distinctUntilChanged");
        observable
            .distinctUntilChanged()
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("  " + s));
    }
}


