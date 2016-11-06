package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

import static io.reactivex.Observable.concat;
import static io.reactivex.Observable.merge;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.output;
import static test.TestUtil.startTest;
import static test.TestUtil.stream;

public class SharedStream {
    public static void main(final String[] args) {
        new SharedStream().runAll();
    }

    public void runAll() {
        final Scheduler scheduler = io();    // io has more threads

        startTest("concat");

        final Observable<Integer> observable =
            stream(20)
                .share();

        // takes all items from the first stream and only when it completes, it starts consuming second stream, etc
        concat(observable.take(5), observable.skip(10).take(5), observable.takeLast(5))
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("                    -> " + s));

        //////////

        final Observable<String> shared =
            stream(8, "shared")
                .subscribeOn(scheduler)
                .share();

        final Observable<String> userA =
            shared.map(s -> "[A]" + s);

        final Observable<String> userB =
            shared.flatMap(s -> stream(2, "[[B]" + s + "]"));

        final Observable<String> userC =
            shared.map(s -> "[C]" + s);

        startTest("shared");
        merge(userA, userB, userC)
            .subscribeOn(scheduler)
            .blockingSubscribe(s -> output("                    -> " + s));
    }
}


