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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.metrigo.client.MetrigoMetricAccumulator;
import org.metrigo.client.MetrigoSendAdapter;

public class MetrigoQueueRunnerImpl implements Runnable {
    private final long samplingIntervalMillis;
    private final ConcurrentHashMap<String, MetrigoMetricAccumulator> accumulators;
    private final MetrigoSendAdapter sendAdapter;

    public MetrigoQueueRunnerImpl(long samplingIntervalMillis, MetrigoSendAdapter sendAdapter) {
        this.samplingIntervalMillis = samplingIntervalMillis;
        this.accumulators = new ConcurrentHashMap<>();
        this.sendAdapter = sendAdapter;
    }

    @Override
    public void run() {
        long adjustmentTime = 0;
        while (true) {
            try {
                Thread.sleep(this.samplingIntervalMillis - adjustmentTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", ie);
            }
            long startTime = System.nanoTime();
            Date startDate = new Date();
            for (Map.Entry<String, MetrigoMetricAccumulator> entry : accumulators.entrySet()) {
                this.sendAdapter.sendMetrics(entry.getKey(), startDate, entry.getValue().sampleStats());
            }
            adjustmentTime = (System.nanoTime() - startTime)/1000000000L;
        }
    }
}
