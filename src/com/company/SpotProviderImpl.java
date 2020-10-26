package com.company;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The straight forward solution with a map of history lists where the key is ccypair value.
 * The history list is based upon tree map in order to get sorting OOTB. That definitely
 * should depend on the read/write ratio, but I believe it's too early to look for any optimizations.
 */
class SpotProviderImpl implements SpotProvider {

    private final Map<CcyPair, SpotsHistory> history = new ConcurrentHashMap<>();

    @Override
    public void add(String ccypair, double spot, LocalDateTime tickTime) {
        findSpotsHistoryBy(ccypair)
                .add(spot, tickTime.toEpochSecond(ZoneOffset.UTC));
    }

    @Override
    public double get(String ccypair, LocalDateTime time) {
        return findSpotsHistoryBy(ccypair)
                .get(time.toEpochSecond(ZoneOffset.UTC));
    }

    private SpotsHistory findSpotsHistoryBy(String ccypair) {
        return history.computeIfAbsent(CcyPair.of(ccypair), k -> new SpotsHistory());
    }

    @Override
    public void init(int days) {
        throw new IllegalArgumentException("not implemented");
    }

    static class CcyPair {

        private final String value;

        private CcyPair(String value) {
            this.value = value;
        }

        public static CcyPair of(String value) {
            return new CcyPair(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CcyPair ccypair = (CcyPair) o;
            return value.equals(ccypair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

    }

    class SpotsHistory {

        private final Map<Long, Double> history = Collections.synchronizedMap(new TreeMap<>());

        public void add(double spot, Long tickTime) {
            history.put(tickTime, spot);
        }

        public double get(Long time) {
            return history.computeIfAbsent(time, this::closestValue);
        }

        private double closestValue(Long time) {
            double result = .0;

            for (Map.Entry<Long, Double> entry : history.entrySet()) {
                if (entry.getKey() <= time)
                    result = entry.getValue();
                else
                    return result;
            }

            return result;
        }

    }

}