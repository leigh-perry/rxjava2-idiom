package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

import java.util.concurrent.TimeUnit;

import static io.reactivex.Observable.error;
import static io.reactivex.Observable.timer;
import static io.reactivex.schedulers.Schedulers.io;
import static java.util.Arrays.asList;
import static test.TestUtil.output;
import static test.TestUtil.startTest;

public class MergeDelayError {
    public static void main(final String[] args) {
        new MergeDelayError().runAll();
    }

    public <T> void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("merge with delay errors");
        final Observable<String> merged =
            Observable.mergeDelayError(
                asList(
                    getObservable(1),
                    getObservable(2),   // -> error
                    getObservable(3),
                    getObservable(4),   // -> error
                    getObservable(5)
                )
            );

        merged
            .subscribeOn(scheduler)
            .blockingSubscribe(TestUtil::output, e -> output("Error [%s]", e.getMessage()));
    }

    private Observable<String> getObservable(final Integer i) {
        return i % 2 == 0
            ? error(new RuntimeException("Error " + i))
            : timer(i * 200, TimeUnit.MILLISECONDS)
                .map(x -> i.toString());
    }
}

