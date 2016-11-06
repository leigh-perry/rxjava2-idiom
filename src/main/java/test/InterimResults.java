package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Predicate;

import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.observeSyncTestOperation;
import static test.TestUtil.output;
import static test.TestUtil.startTest;

public class InterimResults {
    public static void main(final String[] args) {
        new InterimResults().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("interim until");
        final Observable<String> diskCacheFetch =
            observeSyncTestOperation(1, "disk")
                .subscribeOn(scheduler);
        final Observable<String> networkFetch =
            observeSyncTestOperation(2, "network")
                .subscribeOn(scheduler);

        // kick off disk and network fetch together, take disk if arrives first, always finish
        // with network
        diskCacheFetch
            .mergeWith(networkFetch)
            .takeUntil((Predicate<? super String>) result -> result.equals("result:200"))
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("                                " + s));
    }
}



