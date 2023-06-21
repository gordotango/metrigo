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
 * Cross threaded implementation for a metrigo sampler.
 * 
 * This implementation is the thread safe(ish) implementation. It uses the less
 * accurate, but thread "safe" Millis counter from Java. Be careful when using this
 * timer, as it has built in biases, and will likely give you biased results. That
 * said, if you are consistently using this sampler, and you are doing cross thread
 * timing that is typically multiple milli-seconds, you should get reasonable results.
 * 
 * @author Gordon Oliver
 */
public class MetrigoSampleCrossThreadImpl implements MetrigoSample {
    private MetrigoMetricAccumulator errorStats;
    private long startMilliTime;

    /**
     * Constructor.
     */
    public MetrigoSampleCrossThreadImpl() {
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

    private static long MICROS_PER_MILLI = 1000L;

    @VisibleForTesting
    void addSample(MetrigoMetricAccumulator stats, long milliTime) {
        long microTime = milliTime * MICROS_PER_MILLI;
        if (microTime < 0) {
            // This is a hack, and likely incorrect, but there is no "correct"
            // answer here... just that our timer gave us a negative number.
            microTime = 1L;
        }
        stats.addSample(microTime);
        this.errorStats = null;
    }

    @Override
    public void finish(MetrigoMetricAccumulator stats) {
        addSample(stats, System.currentTimeMillis() - this.startMilliTime);
    }

    @Override
    public void start() {
       this.startMilliTime = System.currentTimeMillis();
    }
}
