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

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.metrigo.client.MetrigoMetricAccumulator;

public class MetrigoSampleCrossThreadImplTest {
    @ParameterizedTest
    @CsvSource({"1,1000", "0,0", "2,2000", "-1,1"})
    void testAddSample(long inputNanos, long expected) {
        try (MetrigoSampleCrossThreadImpl underTest = new MetrigoSampleCrossThreadImpl()) {
            MetrigoMetricAccumulator accumulator = mock(MetrigoMetricAccumulator.class);
            underTest.addSample(accumulator, inputNanos);
            verify(accumulator, times(1)).addSample(expected);
            verifyNoMoreInteractions(accumulator);
        }
    }

    @Test
    void testCloseWithError() {
        try (MetrigoSampleCrossThreadImpl underTest = new MetrigoSampleCrossThreadImpl()) {
            MetrigoMetricAccumulator accumulator = mock(MetrigoMetricAccumulator.class);
            underTest.setErrorStats(accumulator);
            underTest.close();
            verify(accumulator, times(1)).addSample(anyLong()); 
            verifyNoMoreInteractions(accumulator);
        }
    }

    @Test
    void testCloseWithoutError() {
        MetrigoSampleCrossThreadImpl underTest = new MetrigoSampleCrossThreadImpl();
        underTest.close();
        // there is really nothing to test here, as there is nothing set.
    }

    @Test
    void testFinishPlusClose() {
        MetrigoSampleCrossThreadImpl underTest = new MetrigoSampleCrossThreadImpl();
        MetrigoMetricAccumulator errorAccumulator = mock(MetrigoMetricAccumulator.class);
        MetrigoMetricAccumulator accumulator = mock(MetrigoMetricAccumulator.class);
        underTest.setErrorStats(errorAccumulator);
        underTest.finish(accumulator);
        underTest.close();
        verify(accumulator, times(1)).addSample(anyLong()); 
        verifyNoMoreInteractions(accumulator);
        verifyNoMoreInteractions(errorAccumulator);
    }

    @Test
    void testStart() {
        MetrigoSampleCrossThreadImpl underTest = new MetrigoSampleCrossThreadImpl();
        MetrigoMetricAccumulator errorAccumulator = mock(MetrigoMetricAccumulator.class);
        MetrigoMetricAccumulator accumulator = mock(MetrigoMetricAccumulator.class);
        underTest.setErrorStats(errorAccumulator);
        underTest.start();
        underTest.finish(accumulator);
        underTest.close();
        verify(accumulator, times(1)).addSample(anyLong()); 
        verifyNoMoreInteractions(accumulator);
        verifyNoMoreInteractions(errorAccumulator);
    }
}