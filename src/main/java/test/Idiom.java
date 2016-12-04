package test;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.reactivex.Observable.amb;
import static io.reactivex.Observable.ambArray;
import static io.reactivex.Observable.combineLatest;
import static io.reactivex.Observable.concat;
import static io.reactivex.Observable.error;
import static io.reactivex.Observable.just;
import static io.reactivex.Observable.merge;
import static io.reactivex.Observable.range;
import static io.reactivex.Observable.timer;
import static io.reactivex.Observable.zip;
import static io.reactivex.schedulers.Schedulers.io;
import static test.TestUtil.output;
import static test.TestUtil.startSection;
import static test.TestUtil.startTiming;

public class Idiom {
    public static final String DELIBERATE_EXCEPTION = "Deliberate exception";

    public static void main(final String[] args) {
        new Idiom().run();
    }

    public void run() {
        final BiFunction<String, String, String> concat = (a, b) -> a + " / " + b;

        // TODO
        // replay
        // join / groupJoin

        final Observable<String> root =
            streamA(16)
                .subscribeOn(io())
                .share();

        startTiming();
        startSection("selective subscriptions");

        class Member {
            Member(final int i, final Observable<String> observable) {
                this.observable = observable;
                this.i = i;
            }

            Observable<String> observable;
            int i;
        }
        final List<Member> observables = new ArrayList<>();
        for (int i = 1; i <= 5; ++i) {
            final int n = i;
            final Observable<String> filtered =
                root
                    .filter(s -> s.hashCode() % n == 1);

            observables.add(new Member(i, filtered));
        }

        final CountDownLatch doneLatch = new CountDownLatch(observables.size());
        observables.forEach(member ->
            member.observable.subscribe(
                value -> output("%s hash %s %s", pad(1), member.i, value),
                t -> output("Exception: %s", t.getMessage()),
                doneLatch::countDown
            )
        );

        try {
            doneLatch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (false) {
            ///////////////////////////////
            run(
                "switchMap",
                streamA(3)
                    .switchMap(s -> {
                        output("switched:%s", s);
                        return streamB(10).subscribeOn(io());
                    })
                    .subscribeOn(io())
            );
            run(
                "switchIfEmpty",
                streamA(0)
                    .switchIfEmpty(streamB(5))
                    .subscribeOn(io())
            );

            run(
                "retryWhen by Ben C",
                hotStream(0, 5, 3)
                    .retryWhen(attempts ->
                        attempts
                            .zipWith(range(1, 3), (n, i) -> i)
                            .doOnEach(x -> System.out.println("dump " + x.getValue()))
                            .flatMap(i -> {
                                    output("delay retry by " + i + " second(s)");
                                    return timer(i, TimeUnit.SECONDS);
                                }
                            )
                    )
            );
            run(
                "retry after time",
                hotStream(0, 5, 3)
                    .retryWhen(exceptions ->
                        exceptions
                            .delay(2, TimeUnit.SECONDS)
                            .take(2)    // limit to 3 tries (2 retries)
                    )
            );
            run("retry count", hotStream(0, 5, 3).retry(3));
            run("repeat count", streamA(5).repeat(3));

            run(
                "groupBy reduced",
                streamA(20)
                    .groupBy(s -> toInt(s) / 4)
                    .flatMapMaybe(e -> e.reduce(concat))
            );

            // window similar buffer; Buffer Observable<T> -> Observable<List<T>>, window returns IObservable<IObservable<T>>
            run("window count", streamA(20).window(4).flatMapMaybe(e -> e.reduce(concat)));
            run("buffer selector", streamA(20).buffer(streamB()));
            run("buffer count/time", streamA(20).buffer(300, TimeUnit.MILLISECONDS, 3));
            run("buffer time", streamA(20).buffer(200, TimeUnit.MILLISECONDS));
            run("buffer count", streamA(20).buffer(4));
            run("amb", ambArray(streamA(), streamB()));
            run("reduce", streamA(10).reduce(concat).toObservable());
            run("debounce msec == throttleWithTimeout", streamA(30).debounce(50, TimeUnit.MILLISECONDS));
            run("scan", streamA(10).scan(concat));
            run("scan", streamA(10).scan(">>", concat));
            run("contains", streamA(30).contains("5").toObservable());
            run("isEmpty", streamA(30).isEmpty().toObservable());

            // TODO: doesn't seem to delay by selected value
            //run("delay selector", streamA(30).delay(s -> just(Long.parseLong(s) * 1000)));

            run("delay msec", streamA(30).delay(1_000, TimeUnit.MILLISECONDS));
            run("zip", zip(streamA(30), streamB(), concat));
            run("withLatestFrom", streamA(30).withLatestFrom(streamB(), concat));
            run("sample time == throttleLast", streamA(30).sample(250, TimeUnit.MILLISECONDS));
            run("throttleFirst", streamA(30).throttleFirst(250, TimeUnit.MILLISECONDS));
            run("sample with another", streamA().sample(streamB()));
            run("merge", merge(streamA(), streamB()));
            run("concat", concat(streamA(), streamB()));
            run("combineLatest", combineLatest(streamA(), hotStream(1, 5, -1), concat));

            run(
                "retry when",
                hotStream(0, 5, 3)
                    .retryWhen(errors ->
                        errors
                            .take(3)    // limit to 3 tries (2 retries)
                            .<Throwable>flatMap(throwable ->
                                throwable.getMessage().equals("Deliberate exception")
                                    ? just(throwable)    // can retry
                                    : error(throwable)
                            )
                    )
            );
        }
    }

    private Integer toInt(final String s) {
        return Integer.valueOf(s.substring(2));
    }

    private void run(final String name, final Observable<?> observable) {
        startTiming();

        startSection(name);

        observable
            .blockingSubscribe(
                value -> {
                    final int id = 2;
                    output("%s%s", pad(id), value);
                },
                t -> output("Exception: %s", t.getMessage())
            );
    }

    private Observable<String> streamA() {
        return streamA(5);
    }

    private Observable<String> streamA(final int count) {
        return hotStream(0, count, -1);
    }

    private Observable<String> streamB() {
        return streamB(5);
    }

    private Observable<String> streamB(final int count) {
        return hotStream(1, count, -1);
    }

    public Observable<String> hotStream(final int id, final int maxCount, final int errorAfter) {
        return Observable.generate(
            () -> 0,
            (i, emitter) -> {
                pause((long) (Math.random() * 100));

                if (i == maxCount) {
                    emitter.onComplete();
                } else if (i == errorAfter) {
                    record(id, "X");
                    emitter.onError(new RuntimeException(DELIBERATE_EXCEPTION));
                } else {
                    final String s = String.valueOf(i);
                    record(id, s);
                    emitter.onNext(s);
                }
                return i + 1;
            }
        ).map(i -> String.format("%s:%s", idOf(id), i));
    }

    private char idOf(final int id) {
        return (char) ('a' + id);
    }

    private void record(final int id, final String s) {
        output("%s%s:%s", pad(id), idOf(id), s);
    }

    private String pad(final int id) {
        String s = "";
        for (int i = 0; i < id; ++i) {
            s += "        ";
        }
        return s;
    }

    private static void pause(final long millis) {
        try {
            // sleep for a random amount of time
            // NOTE: Only using Thread.sleep here as an artificial demo.
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
        }
    }
}



