package org.metrigo;
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

import java.util.List;

/**
 * Metrigo Statistics class.
 * 
 * This class is a highly opinionated set of statistics. It keeps track of a
 * set of counts and totals. The values are binned in a log scale so that we
 * can maintain both dynamic range and detail. It is expected that actual data
 * sets will be sparsely populated.
 */
public class MetrigoStatistics {
    /**
     * The list of lower bounds in microseconds for logarithmic bin accumulation.
     */
    public static final List<Long> LOWER_BOUNDS = List.of(
        0L,
        3L,
        10L,                // 10 microseconds
        31L,
        100L,               // 100 microseconds
        316L,
        1000L,              // 1 msec
        3162L,
        10000L,             // 10 msec
        31622L,
        100000L,            // 100 msec
        316227L,
        1000000L,           // 1 second
        3162276L,
        10000000L,          // 10 seconds
        31622766L,
        100000000L,         // 100 seconds
        316227660L,
        1000000000L         // 1000 seconds
    );

    private List<Long> counts;
    private List<Long> totals;

    /**
     * Construct statistics with values.
     * 
     * @param counts the count of values in each bin.
     * @param totals the total for each bin.
     */
    public MetrigoStatistics(List<Long> counts, List<Long> totals) {
        this.counts = counts;
        this.totals = totals;
    }

    /**
     * get the list of counts.
     * 
     * @return a list of counts. Individual values may be 0, but not null.
     */
    public List<Long> getCounts() {
        return this.counts;
    }

    /**
     * Get the list of totals.
     * 
     * @return a list of totals, Individual values may be 0, but not null.
     */
    public List<Long> getTotals() {
        return this.totals;
    }

    /**
     * Get an array of the counts.
     * 
     * @return an array containing a copy of the counts.
     */
    public long [] getCountsAsArray() {
        return this.counts.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Get an array of the totals.
     * 
     * @return An array with a copy of the totals.
     */
    public long [] getTotalsAsArray() {
        return this.totals.stream().mapToLong(Long::longValue).toArray();
    }
}
