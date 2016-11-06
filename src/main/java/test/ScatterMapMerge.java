package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

import static io.reactivex.Observable.merge;
import static io.reactivex.schedulers.Schedulers.computation;
import static test.TestUtil.observeSyncTestOperation;
import static test.TestUtil.output;
import static test.TestUtil.startTest;
import static test.TestUtil.stream;

public class ScatterMapMerge {
    public static void main(final String[] args) {
        new ScatterMapMerge().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = computation();    // io has more threads

        startTest("scatter map merge");
        final Observable<Observable<String>> scattered =
            stream(15)
                .map(i -> observeSyncTestOperation(i, "")
                    .subscribeOn(scheduler));
        merge(scattered)
            .blockingSubscribe(s -> output("                                " + s));
    }
}



