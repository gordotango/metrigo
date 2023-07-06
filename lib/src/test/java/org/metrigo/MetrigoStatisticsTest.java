package org.metrigo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class MetrigoStatisticsTest {
    private static final Long zero = Long.valueOf(0);
    private static final List<Long> zeroList = List.of(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, zero);
    private static final long [] zeroArray = new long [] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private static final List<Long> countList = List.of(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3), Long.valueOf(4), Long.valueOf(5),
                                                        Long.valueOf(6), Long.valueOf(7), Long.valueOf(8), Long.valueOf(9), Long.valueOf(10),
                                                        Long.valueOf(11), Long.valueOf(12), Long.valueOf(13), Long.valueOf(14), Long.valueOf(15),
                                                        Long.valueOf(16), Long.valueOf(17), Long.valueOf(18), Long.valueOf(19));
    private static final long [] countArray = new long [] {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L };

    @Test
    void testGetCounts() {
        List<Long> expected = countList;
        MetrigoStatistics underTest = new MetrigoStatistics(expected, zeroList);
        assertThat(underTest.getCounts(), sameInstance(expected));
        assertThat(underTest.getTotals(), sameInstance(zeroList));
    }

    @Test
    void testGetCountsAsArray() {
        MetrigoStatistics underTest = new MetrigoStatistics(countList, zeroList);
        assertThat(underTest.getCountsAsArray(), equalTo(countArray));
        assertThat(underTest.getTotalsAsArray(), equalTo(zeroArray));
    }

    @Test
    void testGetTotals() {
        List<Long> expected = countList;
        MetrigoStatistics underTest = new MetrigoStatistics(zeroList, expected);
        assertThat(underTest.getTotals(), sameInstance(expected));
    }

    @Test
    void testGetTotalsAsArray() {
        MetrigoStatistics underTest = new MetrigoStatistics(zeroList, countList);
        assertThat(underTest.getTotalsAsArray(), equalTo(countArray));
    }

    private static final List<Arguments> longSerializationArguments = List.of(
        Arguments.of(1L, 0, new byte[] { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(1L, 1, new byte[] { 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x100L, 0, new byte[] { 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x1000L, 0, new byte[] { 2, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x10000L, 0, new byte[] { 3, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x1000000L, 0, new byte[] { 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x100000000L, 0, new byte[] { 5, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x10000000000L, 0, new byte[] { 6, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x1000000000000L, 0, new byte[] { 7, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(0x100000000000000L, 0, new byte[] { 8, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 }),
        Arguments.of(Long.MAX_VALUE, 0, new byte[] { 8, -1, -1, -1, -1, -1, -1, -1, 127, 0, 0, 0, 0, 0, 0 })
    );

    private static class LongSerializationPassArgumentProvider implements ArgumentsProvider {
        public Stream<Arguments> provideArguments(ExtensionContext ignored) {
            return longSerializationArguments.stream();
        }
    };

    @ParameterizedTest
    @ArgumentsSource(value = LongSerializationPassArgumentProvider.class)
    void testSerializeLong(long value, int position, byte [] expected) {
        byte [] result = new byte [expected.length];
        Arrays.fill(result, 0, result.length, (byte)0);
        MetrigoStatistics.serializeLong(value, result, position);
        assertThat(result, equalTo(expected));
    }

    @ParameterizedTest
    @ArgumentsSource(value = LongSerializationPassArgumentProvider.class)
    void testDeserializeLong(long value, int position, byte [] expected) {
        List<Long> appendable = new ArrayList<>();
        MetrigoStatistics.deserializeLongAndAppend(expected, position, appendable);
        assertThat(appendable, contains(Long.valueOf(value)));
    }

    private static final List<Arguments> longSerializationFailArguments = List.of(
        Arguments.of(new byte [] { -1 }, 0,
                     "Failed to parse stream at position 0 expected length less than 8 but got 255"),
        Arguments.of(new byte [] { 5, 0 }, 0,
                     "Failed to parse stream at position 0 needed 5 bytes on stream, but have 2")
    );

    private static class LongSerializationFailArgumentProvider implements ArgumentsProvider {
        public Stream<Arguments> provideArguments(ExtensionContext ignored) {
            return longSerializationFailArguments.stream();
        }
    };

    @ParameterizedTest
    @ArgumentsSource(value = LongSerializationFailArgumentProvider.class)
    void testDeserializeLongFail(byte [] input, int position, String expectedError) {
        List<Long> appendable = new ArrayList<>();
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                                                    () -> MetrigoStatistics.deserializeLongAndAppend(input, position, appendable));
        assertThat(iae.getMessage(), equalTo(expectedError));
    }

    private static final List<Arguments> statsSerializationArguments = List.of(
        // A simple value at the start
        Arguments.of(
            new MetrigoStatistics(
                List.of(Long.valueOf(1), zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, zero),
                List.of(Long.valueOf(2), zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, zero)
            ),
            new byte [] {
                0, 1, 1, 1, 2
            }
        ),
        // A value at the start and end
        Arguments.of(
            new MetrigoStatistics(
                List.of(Long.valueOf(1), zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(2)),
                List.of(Long.valueOf(2), zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(254))
            ),
            new byte [] {
                0, 1, 1, 1, 2, 18, 1, 2, 1, (byte)254
            }
        ),
        // A value at the end
        Arguments.of(
            new MetrigoStatistics(
                List.of(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(2)),
                List.of(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(254))
            ),
            new byte [] {
                18, 1, 2, 1, (byte)254
            }
        ),
        // A large value at the end
        Arguments.of(
            new MetrigoStatistics(
                List.of(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(2)),
                List.of(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(1000000000L))
            ),
            new byte [] {
                18, 1, 2, 4, 0, -54, -102, 59
            }
        ),
        // A power of 2 at the end
        Arguments.of(
            new MetrigoStatistics(
                List.of(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(2)),
                List.of(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero,
                        zero, zero, zero, zero, zero, zero, zero, zero, Long.valueOf(0x1000000000000L))
            ),
            new byte [] {
                18, 1, 2, 7, 0, 0, 0, 0, 0, 0, 1
            }
        )
    );

    private static class StatsSerializationPassArgumentProvider implements ArgumentsProvider {
        public Stream<Arguments> provideArguments(ExtensionContext ignored) {
            return statsSerializationArguments.stream();
        }
    };

    @ParameterizedTest
    @ArgumentsSource(value = StatsSerializationPassArgumentProvider.class)
    void testToBytes(MetrigoStatistics stats, byte [] serialized) {
        assertThat(MetrigoStatistics.toBytes(stats), equalTo(serialized));
    }

    @ParameterizedTest
    @ArgumentsSource(value = StatsSerializationPassArgumentProvider.class)
    void testFromBytes(MetrigoStatistics stats, byte [] serialized) {
        MetrigoStatistics deserialized = MetrigoStatistics.fromBytes(serialized);
        assertThat(deserialized.getCounts(), equalTo(stats.getCounts()));
        assertThat(deserialized.getTotals(), equalTo(stats.getTotals()));
        assertThat(MetrigoStatistics.toBytes(deserialized), equalTo(serialized));
    }

    private static final List<Arguments> statsSerializationFailArguments = List.of(
        Arguments.of(new byte [] { 1, -1 },
                     "Failed to parse stream at position 1 expected length less than 8 but got 255"),
        Arguments.of(new byte [] { 2, 5, 0 },
                     "Failed to parse stream at position 1 needed 5 bytes on stream, but have 3"),
        Arguments.of(new byte [] { 2, 1, 0 },
                     "Failed to parse stream at position 3 unexpected end of stream"),
        Arguments.of(new byte [] { 2, 1, 1, 1, 1, 1, 1, 0, 1, 0 },
                     "Failed to parse stream at 5 the location 1 was out of bounds [3, 19)"),
        Arguments.of(new byte [] { 19, 1, 1, 1, 1, 1, 1, 0, 1, 0 },
                     "Failed to parse stream at 0 the location 19 was out of bounds [0, 19)")
    );

    private static class StatsSerializationFailArgumentProvider implements ArgumentsProvider {
        public Stream<Arguments> provideArguments(ExtensionContext ignored) {
            return statsSerializationFailArguments.stream();
        }
    };

    @ParameterizedTest
    @ArgumentsSource(value = StatsSerializationFailArgumentProvider.class)
    void testFromBytesFail(byte [] serialized, String expectedError) {
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                                                    () -> MetrigoStatistics.fromBytes(serialized));
        assertThat(iae.getMessage(), equalTo(expectedError));
    }
}
