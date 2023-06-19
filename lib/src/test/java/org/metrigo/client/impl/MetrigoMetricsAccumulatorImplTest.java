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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.metrigo.MetrigoStatistics;

public class MetrigoMetricsAccumulatorImplTest {
    @ParameterizedTest
    @CsvSource({ "0,0", "2,0", "3,1", "9,1", "10,2", "30,2", "31,3", "99,3", "100,4", "315,4",
                 "316,5", "999,5", "1000,6", "3161,6", "3162,7", "9999,7", "10000,8", "31621,8",
                 "31622,9", "99999,9", "100000,10", "316226,10", "316227,11", "999999,11",
                 "1000000,12", "3162275,12", "3162276,13", "9999999,13", "10000000,14", "31622765,14",
                "31622766,15", "99999999,15", "100000000,16", "316227659,16", "316227660,17", "999999999,17",
                 "1000000000,18", "10000000000,18" })
    public void testBins(long value, int expectedBin) {
        assertEquals(expectedBin, MetrigoMetricsAccumulatorImpl.getBin(value));
    }

    @Test
    public void testGetName() {
        String metricName = "theName";

        MetrigoMetricsAccumulatorImpl underTest = new MetrigoMetricsAccumulatorImpl(metricName);
        assertSame(metricName, underTest.getMetricName());
    }

    @Test
    public void testNoSamples() {
        MetrigoMetricsAccumulatorImpl underTest = new MetrigoMetricsAccumulatorImpl("theName");
        assertNull(underTest.sampleStats());   
    }

    @Test
    public void testOneSample() {
        MetrigoMetricsAccumulatorImpl underTest = new MetrigoMetricsAccumulatorImpl("theName");
        underTest.addSample(2);
        MetrigoStatistics result = underTest.sampleStats();
        // verify that we cleared the stats.
        assertNull(underTest.sampleStats());
        Long zero = Long.valueOf(0);
        assertThat(result.getCounts(), contains(Long.valueOf(1), zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero));
        assertThat(result.getTotals(), contains(Long.valueOf(2), zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero));
    }

    @Test
    public void testTwoSamplesOneBin() {
        MetrigoMetricsAccumulatorImpl underTest = new MetrigoMetricsAccumulatorImpl("theName");
        underTest.addSample(2);
        underTest.addSample(1);
        MetrigoStatistics result = underTest.sampleStats();
        // verify that we cleared the stats.
        assertNull(underTest.sampleStats());
        Long zero = Long.valueOf(0);
        assertThat(result.getCounts(), contains(Long.valueOf(2), zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero));
        assertThat(result.getTotals(), contains(Long.valueOf(3), zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero));
    }
}
