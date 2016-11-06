package test;

import io.reactivex.Completable;
import io.reactivex.Scheduler;

import static io.reactivex.Completable.defer;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.startTest;
import static test.TestUtil.syncTestOperation;

public class CompletableTest {
    public static void main(final String[] args) {
        new CompletableTest().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("single");
        final Completable syncOperation =
            Completable.fromAction(() -> syncTestOperation(1, ""));

        syncOperation
            .subscribeOn(scheduler)
            .blockingAwait();
    }
}


