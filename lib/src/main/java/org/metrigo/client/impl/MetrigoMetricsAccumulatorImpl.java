package org.metrigo.client.impl;
/*
 * This file is part of Metrigo.
 * 
 * Metrigo is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Metrigo is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with Metrigo. If not, see <https://www.gnu.org/licenses/>.
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.metrigo.MetrigoStatistics;
import org.metrigo.client.MetrigoMetricAccumulator;

import com.google.common.annotations.VisibleForTesting;

/**
 * Metrics accumulator implementation.
 * 
 * This accumulator is a "hidden" piece of the code. Consumers should generally not interact with this directly,
 * but rather interact with a sampler. This accumulator will accumulate a logarithmic histogram that can be sent
 * out to a metrics storage.
 */ 
public class MetrigoMetricsAccumulatorImpl implements MetrigoMetricAccumulator {
    private static final long lowerBounds [] = MetrigoStatistics.LOWER_BOUNDS.stream().mapToLong(Long::longValue).toArray();

    /**
     * Internal class to actually accumulate statistics.
     * 
     * This internal class is used to ensure
     * * the stats are atomically initialized.
     * * the stats can be swapped out easily.
     * * the stats are fast to update.
     * The stats are held in an array to speed updates, but must be synchronized so
     * that they are correct.
     */
    private static class StatsHolder {
        private long [] counts;
        private long [] totals;
        private long statsCount;

        /**
         * Constructor.
         */
        public StatsHolder() {
            this.counts = new long[lowerBounds.length];
            this.totals = new long[lowerBounds.length];

            for (int i = 0; i < lowerBounds.length; i++) {
                this.counts[i] = 0;
                this.totals[i] = 0;
            }
        }

        /**
         * Add a total to a specific bin.
         * 
         * @param bin the bin to update.
         * @param total the number of microseconds to add.
         */
        public void addToBin(int bin, long total) {
            this.counts[bin] += 1;
            this.totals[bin] += total;
            this.statsCount += 1;
        }

        /**
         * Convert this holder into a public structure.
         * 
         * @return the converted stats.
         */
        @CheckForNull
        public MetrigoStatistics convertToStats() {
            if (this.statsCount == 0) {
                // Nothing here.
                return null;
            }
            List<Long> countList = Arrays.stream(this.counts).boxed().collect(Collectors.toList());
            List<Long> totalsList = Arrays.stream(this.totals).boxed().collect(Collectors.toList());
            return new MetrigoStatistics(countList, totalsList);
        }
    }
    
    private volatile StatsHolder stats;
    @Nonnull private final String name;

    /**
     * Constructor.
     * 
     * @param metricName the name for this metric.
     */
    public MetrigoMetricsAccumulatorImpl(@Nonnull String metricName) {
        this.stats = new StatsHolder();
        this.name = metricName;
    }

    /**
     * Get the bin number for a specific time.
     * 
     * Because the bins are logarithmically scaled, we need to do a binary search.
     * 
     * @param timeInMicroSeconds the time that we are binning.
     * @return the bin number.
     */
    @VisibleForTesting
    static int getBin(long timeInMicroSeconds) {
        int lower = 0;
        int higher = lowerBounds.length - 1;

        do {
            int guess = (higher + lower)/2;
            if (timeInMicroSeconds >= lowerBounds[guess]) {
                lower = guess;
            } else {
                higher = guess;
            }
        } while (higher - lower > 1);
        if (timeInMicroSeconds >= lowerBounds[higher]) {
            return higher;
        } else {
            return lower;
        }
    }

    @Override
    @Nonnull
    public String getMetricName() {
        return this.name;
    }

    @Override
    public void addSample(long timeInMicroSeconds) {
        int bin = getBin(timeInMicroSeconds);
        synchronized (this) {
            this.stats.addToBin(bin, timeInMicroSeconds);
        }
    }

    @Override
    public MetrigoStatistics sampleStats() {
        StatsHolder newStats = new StatsHolder();
        StatsHolder oldStats = this.stats;
        synchronized (this) {
            this.stats = newStats;
        }
        return oldStats.convertToStats();
    }
}