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

import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

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
        if (counts.size() != LOWER_BOUNDS.size()) {
            throw new IllegalArgumentException("Length of counts is " + counts.size() + " but we expected "+ LOWER_BOUNDS.size());
        }
        if (totals.size() != LOWER_BOUNDS.size()) {
            throw new IllegalArgumentException("Length of totals is " + totals.size() + " but we expected "+ LOWER_BOUNDS.size());
        }
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

    @Override
    public String toString() {
        return "MetrigoStatistics [counts=" + counts + ", totals=" + totals + "]";
    }

    /**
     * Get an array of the totals.
     * 
     * @return An array with a copy of the totals.
     */
    public long [] getTotalsAsArray() {
        return this.totals.stream().mapToLong(Long::longValue).toArray();
    }

    /**
     * Serialize a long avlue into an array at a position.
     * 
     * @param value the value to serialize.
     * @param holder the array to serialize into.
     * @param position the position to serialize into
     * @return the new end position.
     */
    @VisibleForTesting
    static int serializeLong(long value, byte [] holder, int position) {
        int npos = position;
        int lengthSpot = npos++;
        int valueLen = 0;
        long modValue = value;
        do {
            holder[npos++] = (byte)(modValue & 0xff);
            valueLen += 1;
            modValue >>= 8;
        } while (modValue != 0);
        holder[lengthSpot] = (byte)valueLen;
        return npos;
    }

    /**
     * Deserialize a single long value `
     * 
     * @param holder the byte array that holds the value.
     * @param position the starting position to serialize from
     * @param appendable the list to append the new value onto.
     * @return the next start position
     */
    @VisibleForTesting
    static int deserializeLongAndAppend(byte [] holder, int position, List<Long> appendable) {
        int npos = position;

        if (npos >= holder.length) {
            throw new IllegalArgumentException("Failed to parse stream at position " + position
                                               + " unexpected end of stream");
        }

        int valueLen = ((int)holder[npos++] & 0xff);
        int shift = 0;
        long value = 0;
        if (valueLen > Long.BYTES) {
            throw new IllegalArgumentException("Failed to parse stream at position " + position
                                               + " expected length less than " + Long.BYTES
                                               + " but got " + valueLen);
        }
        if (npos + valueLen > holder.length) {
            throw new IllegalArgumentException("Failed to parse stream at position " + position
                                               + " needed " + valueLen
                                               + " bytes on stream, but have " + holder.length);
        }

        while (valueLen > 0) {
            value |= (((long)holder[npos++] & 0xff) << shift);
            shift += 8;
            valueLen -= 1;
        }
        appendable.add(Long.valueOf(value));
        return npos;
    }

    /**
     * The maximum number of bytes to encode a single entry.
     * 
     * * one byte for position.
     * * one byte for length
     * * {@link Long.BYTES} for the actual value.
     */
    static int MAX_BYTES_PER_ENTRY = (Long.BYTES + 2);

    /**
     * Serialize a MetrigoStatistics instance into a byte array.
     * 
     * @param value the value that we should serialize.
     * @return the serialized byte array.
     */
    public static byte [] toBytes(MetrigoStatistics value) {
        int maxLen = LOWER_BOUNDS.size() * 2 * MAX_BYTES_PER_ENTRY;
        byte [] holder = new byte [ maxLen ];
        int actualLen = 0;
        for (int i = 0; i < LOWER_BOUNDS.size(); i++) {
            long count = value.counts.get(i).longValue();
            if (count != 0) {
                holder[actualLen++] = (byte) i;
                actualLen = serializeLong(count, holder, actualLen);
                actualLen = serializeLong(value.totals.get(i).longValue(), holder, actualLen);
            }
        }
        byte [] result = new byte[actualLen];
        for (int i = 0; i < actualLen; i++) {
            result[i] = holder[i];
        }
        return result;
    }

    /**
     * Deserialize a MetrigoStatistics instance from a byte array.
     * 
     * @param bytes the byte stream to deserialize
     * @return the deserialized instance.
     */
    public static MetrigoStatistics fromBytes(byte [] bytes) {
        ArrayList<Long> counts = new ArrayList<>();
        ArrayList<Long> totals = new ArrayList<>();
        int i = 0;

        while (i < bytes.length) {
            int position = (((int)bytes[i++]) & 0xff);
            if (position < counts.size() || position >= LOWER_BOUNDS.size()) {
                throw new IllegalArgumentException("Failed to parse stream at " + (i-1)
                                                   + " the location " + position
                                                   + " was out of bounds [" + counts.size() + ", " + LOWER_BOUNDS.size() + ")");
            }
            while (counts.size() < position) {
                counts.add(Long.valueOf(0));
                totals.add(Long.valueOf(0));
            }
            i = deserializeLongAndAppend(bytes, i, counts);
            i = deserializeLongAndAppend(bytes, i, totals);
        }
        while (counts.size() < LOWER_BOUNDS.size()) {
            counts.add(Long.valueOf(0));
            totals.add(Long.valueOf(0));
        }
        return new MetrigoStatistics(counts, totals);
    }
}
