package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.observeSyncTestOperation;
import static test.TestUtil.output;
import static test.TestUtil.startTest;

public class FirstWins {
    public static void main(final String[] args) {
        new FirstWins().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("first wins");
        final Observable<String> syncOperation1 =
            observeSyncTestOperation(1, "")
                .subscribeOn(scheduler);
        final Observable<String> syncOperation2 =
            observeSyncTestOperation(2, "")
                .subscribeOn(scheduler);

        String s = syncOperation1
            .mergeWith(syncOperation2)
            .firstElement()
            .subscribeOn(scheduler)
            .blockingGet();
        output("                                " + s);
    }
}


