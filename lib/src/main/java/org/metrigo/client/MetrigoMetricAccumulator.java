package org.metrigo.client;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.metrigo.MetrigoStatistics;

/**
 * A metrics accumulator interface.
 * 
 * This interface should generally just use the standard implementation, as it is not
 * something that a consumer would want to override.
 */
public interface MetrigoMetricAccumulator {
    /**
     * Get the name of this metric (should be unique).
     * 
     * @return the name.
     */
    @Nonnull
    String getMetricName();

    /**
     * Add a sample to the accumulator.
     * 
     * This method is typically called from a sampler when it is closed.
     * 
     * @param timeInMicroSeconds the sample time.
     */
    void addSample(long timeInMicroSeconds);

    /**
     * Get the current sample set.
     * 
     * This method returns a set of statistics that has been accumulated
     * since the last call to sampleStats (or the initialization of the
     * accumulator). This will then start a fresh set of statistics gathering.
     * 
     * @return the current set of statistics, swapping for a fresh set.
     */
    @CheckForNull
    MetrigoStatistics sampleStats();
}
