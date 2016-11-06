package test;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static test.TestUtil.output;
import static test.TestUtil.pause;
import static test.TestUtil.startTest;

public class TrampolineScheduler {
    public static void main(final String[] args) {
        new TrampolineScheduler().runAll();
    }

    public <T> void runAll() {
        startTest("trampoline");
        test(Schedulers.trampoline());
    }

    private void test(final Scheduler scheduler) {
        final Scheduler.Worker worker = scheduler.createWorker();

        output("Main start");
        worker.schedule(() -> {
            output("  Outer start");
            pause(1000);
            worker.schedule(() -> {
                output("    Middle start");
                pause(1000);
                worker.schedule(() -> {
                    output("      Inner start");
                    pause(1000);
                    output("      Inner end");
                });
                output("    Middle end");
            });
            output("  Outer end");
        });
        output("Main end");
    }
}

