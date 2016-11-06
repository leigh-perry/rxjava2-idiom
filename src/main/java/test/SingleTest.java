package test;

import io.reactivex.Scheduler;
import io.reactivex.Single;

import static io.reactivex.Single.defer;
import static io.reactivex.Single.just;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.output;
import static test.TestUtil.startTest;
import static test.TestUtil.syncTestOperation;

public class SingleTest {
    public static void main(final String[] args) {
        new SingleTest().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("single");
        final Single<String> syncOperation =
            defer(() -> just(syncTestOperation(1, "")))
                .subscribeOn(scheduler);

        final String s = syncOperation
            .subscribeOn(scheduler)
            .blockingGet();
        output("                                " + s);
    }
}


