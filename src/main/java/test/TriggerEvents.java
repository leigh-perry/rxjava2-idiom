package test;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import java.util.concurrent.TimeUnit;

import static test.TestUtil.output;
import static test.TestUtil.startTest;

public class TriggerEvents {
    public static void main(final String[] args) {
        new TriggerEvents().run();
    }

    public void run() {
        startTest("trigger events");
        final PublishSubject<Integer> paginator = PublishSubject.create();

        paginator
            .concatMap(pageNo -> TestUtil.observeSyncTestOperation(pageNo, "fetch"))
            .subscribeOn(Schedulers.io())
            .subscribe(s -> output("                                " + s));

        Observable.interval(1000, 5000, TimeUnit.MILLISECONDS)
            .take(5)
            .blockingSubscribe(i -> paginator.onNext((int) (long) i));
    }
}
