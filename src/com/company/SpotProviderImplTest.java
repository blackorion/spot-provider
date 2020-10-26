package com.company;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static java.lang.String.format;

/**
 * Spot Provider Test class is added with handmade validation system because of the restrictions
 * applied to use 3rd-party libs
 */
class SpotProviderImplTest {

    public static final String USDRUB = "USDRUB";
    public static final String EURRUB = "EURRUB";
    public static final LocalDateTime t1 = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    public static final LocalDateTime t2 = LocalDateTime.of(2020, 1, 1, 0, 0, 1);
    public static final LocalDateTime t3 = LocalDateTime.of(2020, 1, 1, 0, 0, 2);
    public static final LocalDateTime t4 = LocalDateTime.of(2020, 1, 1, 0, 0, 3);

    public static void main(String[] args) throws InterruptedException {
        new SpotProviderImplTest()
                .runTests()
                .runAsyncTests();
    }

    public SpotProviderImplTest runTests() {
        SpotProvider sp = new SpotProviderImpl();
        assertEqual(sp.get(USDRUB, t1), .0);

        sp = new SpotProviderImpl();
        sp.add(USDRUB, 1.1, t1);
        assertEqual(sp.get(USDRUB, t1), 1.1);
        assertEqual(sp.get(EURRUB, t1), .0);

        sp = new SpotProviderImpl();
        sp.add(USDRUB, 1.1, t1);
        sp.add(USDRUB, 1.2, t2);
        sp.add(USDRUB, 1.3, t3);
        assertEqual(sp.get(USDRUB, t1), 1.1);
        assertEqual(sp.get(USDRUB, t2), 1.2);
        assertEqual(sp.get(USDRUB, t3), 1.3);

        sp = new SpotProviderImpl();
        sp.add(USDRUB, 1.1, t1);
        sp.add(USDRUB, 1.3, t3);
        assertEqual(sp.get(USDRUB, t2), 1.1);

        sp = new SpotProviderImpl();
        sp.add(USDRUB, 1.1, t1);
        sp.add(USDRUB, 1.3, t3);
        sp.add(EURRUB, 1.2, t2);
        sp.add(EURRUB, 1.4, t4);
        assertEqual(sp.get(USDRUB, t4), 1.3);
        assertEqual(sp.get(EURRUB, t4), 1.4);

        sp = new SpotProviderImpl();
        sp.add(USDRUB, 1.1, t1);
        sp.add(USDRUB, 1.3, t4);
        sp.add(USDRUB, 1.2, t2);
        assertEqual(sp.get(USDRUB, t3), 1.2);

        return this;
    }

    private SpotProviderImplTest runAsyncTests() throws InterruptedException {
        final SpotProvider sp1 = new SpotProviderImpl();
        runConcurrently(
                () -> sp1.add(USDRUB, 1.1, t1),
                () -> {
                    sleep(30);
                    assertEqual(sp1.get(USDRUB, t1), 1.1);
                }
        );

        final SpotProvider sp2 = new SpotProviderImpl();
        runConcurrently(
                () -> {
                    IntStream.range(0, 60)
                            .filter(i -> i % 2 == 0)
                            .forEach(i -> sp2.add(USDRUB, i * 1.1, LocalDateTime.of(2020, 1, 1, 0, 0, i)));
                },
                () -> {
                    IntStream.range(0, 60)
                            .filter(i -> i % 2 == 1)
                            .forEach(i -> sp2.add(USDRUB, i * 1.1, LocalDateTime.of(2020, 1, 1, 0, 0, i)));

                    sleep(1000);

                    IntStream.range(0, 60)
                            .forEach(i -> assertEqual(sp2.get(USDRUB, LocalDateTime.of(2020, 1, 1, 0, 0, i)), i * 1.1));
                }
        );

        return this;
    }

    void assertEqual(double actual, double expected) {
        if (Math.abs(actual - expected) > .01)
            throw new RuntimeException(format("expected: %.2f, actual: %.2f", expected, actual));
    }

    private void runConcurrently(Runnable fn1, Runnable fn2) throws InterruptedException {
        Thread tr1 = new Thread(fn1);
        Thread tr2 = new Thread(fn2);

        tr1.start();
        tr2.start();

        tr1.join();
        tr2.join();
    }

    private void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}