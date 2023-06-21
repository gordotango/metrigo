package org.metrigo.client.impl;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.metrigo.client.MetrigoMetricAccumulator;

public class MetrigoSampleSingleThreadImplTest {
    @ParameterizedTest
    @CsvSource({"1,0", "500000,0", "500001,1", "1499999,1",
                "1500000,2", "2500000,2"})
    void testAddSample(long inputNanos, long expected) {
        MetrigoSampleSingleThreadImpl underTest = new MetrigoSampleSingleThreadImpl();
        MetrigoMetricAccumulator accumulator = mock(MetrigoMetricAccumulator.class);
        underTest.addSample(accumulator, inputNanos);
        verify(accumulator, times(1)).addSample(expected);
        verifyNoMoreInteractions(accumulator);
    }

    @Test
    void testCloseWithError() {
        MetrigoSampleSingleThreadImpl underTest = new MetrigoSampleSingleThreadImpl();
        MetrigoMetricAccumulator accumulator = mock(MetrigoMetricAccumulator.class);
        underTest.setErrorStats(accumulator);
        underTest.close();
        verify(accumulator, times(1)).addSample(anyLong()); 
        verifyNoMoreInteractions(accumulator);
    }

    @Test
    void testCloseWithoutError() {
        MetrigoSampleSingleThreadImpl underTest = new MetrigoSampleSingleThreadImpl();
        underTest.close();
        // there is really nothing to test here, as there is nothing to do.
    }

    @Test
    void testFinishPlusClose() {
        MetrigoSampleSingleThreadImpl underTest = new MetrigoSampleSingleThreadImpl();
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
        MetrigoSampleSingleThreadImpl underTest = new MetrigoSampleSingleThreadImpl();
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
