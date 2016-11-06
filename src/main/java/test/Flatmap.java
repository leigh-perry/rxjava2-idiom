package test;

import io.reactivex.Scheduler;

import static io.reactivex.Observable.range;
import static io.reactivex.schedulers.Schedulers.computation;
import static test.TestUtil.output;
import static test.TestUtil.startTest;
import static test.TestUtil.stream;

public class Flatmap {
    public static void main(final String[] args) {
        new Flatmap().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = computation();    // io has more threads

        startTest("flatmap");
        stream(8)
            .flatMap(i -> range(1, i + 1).map(j -> "v" + i + ":" + j))
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("            " + s));
    }
}



