package test;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

import static io.reactivex.Observable.fromArray;
import static io.reactivex.Observable.timer;

public class Retry {
    public static <T> Observable<T> observeWithRetry(final Observable<T> observable, final Integer[] backoffMsecs) {
        return observable
            .retryWhen(exceptions ->
                exceptions
                    .zipWith(fromArray(backoffMsecs), (exception, backoffMsec) -> backoffMsec)
                    .flatMap(backoffMsec -> timer(backoffMsec, TimeUnit.MILLISECONDS))
            );
    }
}


