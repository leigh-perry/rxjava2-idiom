package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

import static io.reactivex.Observable.defer;
import static io.reactivex.Observable.merge;
import static io.reactivex.Observable.switchOnNext;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.output;
import static test.TestUtil.startTest;
import static test.TestUtil.stream;

public class SwitchSearchResults {
    public static void main(final String[] args) {
        new SwitchSearchResults().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        final Observable<String> searchValues = stream(3, "input");

        final Observable<Observable<String>> search =
            searchValues
                .subscribeOn(scheduler)
                .map(this::search);

        startTest("merge");
        merge(search)
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("                    -> " + s));

        startTest("switch");
        switchOnNext(search)
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("                    -> " + s));
    }

    private Observable<String> search(final String query) {
        return defer(() -> stream(2, query + " res")
            .subscribeOn(io()));
    }
}


