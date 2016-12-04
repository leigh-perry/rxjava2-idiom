package test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.observables.ConnectableObservable;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.reactivex.schedulers.Schedulers.io;

public class ReplayTimeWindow {
    private final long startTime = System.currentTimeMillis();

    public static void main(final String[] args) {
        new ReplayTimeWindow().run();
    }

    static final int b = 2;

    interface F3 extends Function<Integer, Function<Integer, Function<Integer, Integer>>> {
    }

    private Stream<Integer> calculate(final Stream<Integer> stream, final Integer a) {
        final F3 calculation = x -> y -> z -> x + y * z;
        return stream.map(calculation.apply(b).apply(a));
    }


    public void run() {
        final Scheduler scheduler = io();

        class Event {
            final long seq;
            final long creationEpoch;

            public Event(final long seq) {
                this.seq = seq;
                creationEpoch = System.currentTimeMillis();
            }

            @Override
            public String toString() {
                return "E{seq=" + seq + '}';
            }

            public long age() {
                return System.currentTimeMillis() - creationEpoch;
            }
        }

        final long multiplier = 1;
        final long window = 55 * multiplier;

        final ConnectableObservable<Event> replayable =
            Observable.interval(50 * multiplier, TimeUnit.MILLISECONDS)
                .map(seq -> new Event(seq))
                //.doOnEach(n -> System.out.println(record("-----Replayable %s-----", n.getValue())))
                .subscribeOn(scheduler)
                .replay(window, TimeUnit.MILLISECONDS, scheduler);

        final Observable<String> stringObservable =
            Observable.interval(50 * multiplier, TimeUnit.MILLISECONDS)
                .subscribeOn(scheduler)
                //.doOnEach(n -> System.out.println(record("-----Tick %s-----------", n.getValue())))
                .flatMap(value ->
                    replayable
                        .filter(e -> e.age() >= window)
                        .map(e -> record("tick %s: event %s age %s msec", value, e, e.age()))
                )
                .subscribeOn(scheduler);

        replayable.connect();

        stringObservable
            .blockingSubscribe(System.out::println);
    }

    private String record(final String format, final Object... args) {
        return String.format("%3d %4dms ", Thread.currentThread().getId(), System.currentTimeMillis() - startTime)
            + String.format(format, args);
    }
}

