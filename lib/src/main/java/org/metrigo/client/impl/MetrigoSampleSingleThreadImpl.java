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

import org.metrigo.client.MetrigoMetricAccumulator;
import org.metrigo.client.MetrigoSample;

import com.google.common.annotations.VisibleForTesting;

/**
 * Single threaded implementation for a metrigo sampler.
 * 
 * This implementation is the non-thread safe implementation. It uses the more
 * accurate, but thread specific Nano Time counter from Java. There is careful
 * logic to avoid biasing down due to rounding.
 * 
 * @author Gordon Oliver
 */
public class MetrigoSampleSingleThreadImpl implements MetrigoSample {
    private MetrigoMetricAccumulator errorStats;
    private long startNanoTime;

    /**
     * Constructor.
     */
    public MetrigoSampleSingleThreadImpl() {
    }

    /**
     * Set the error statistics for this sample.
     * 
     * @param errorStats the new error stats to set.
     */
    public void setErrorStats(final MetrigoMetricAccumulator errorStats) {
        this.errorStats = errorStats;
    }

    @Override
    public void close() {
         if (this.errorStats != null) {
            finish(this.errorStats);
         }
    }

    private static long NANOS_PER_MICRO = 1000000L;
    private static long NANOS_FOR_ROUND_EVEN = 499999L;
    private static long NANOS_FOR_ROUND_ODD = 500000L;

    @VisibleForTesting
    void addSample(MetrigoMetricAccumulator stats, long nanoTime) {
        long microTime = (nanoTime + NANOS_FOR_ROUND_EVEN) / NANOS_PER_MICRO;
        if (microTime % 2 == 1) {
            microTime = (nanoTime + NANOS_FOR_ROUND_ODD) / NANOS_PER_MICRO;
        }
        stats.addSample(microTime);
        this.errorStats = null;
    }

    @Override
    public void finish(MetrigoMetricAccumulator stats) {
        addSample(stats, System.nanoTime() - this.startNanoTime);
    }

    @Override
    public void start() {
       this.startNanoTime = System.nanoTime();
    }
}
