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

import org.metrigo.client.impl.MetrigoSampleCrossThreadImpl;
import org.metrigo.client.impl.MetrigoSampleSingleThreadImpl;

/**
  * Public interface to get default implementations.
  */
public class MetrigoClient {
    /**
     * Get a single threaded sampler.
     * 
     * This method will get and populate a single threaded sampler. This should be used in a stanza like
     * <pre>
     *     try (MetrigoSample sample = MetrigoClient.getSamplerAndStart(errorAccumulator)) {
     *         //.. do stuff.
     *         sample.finish(successAccumulator);
     *     }
     * </pre>
     * 
     * @param errorAccumulator the accumulator to use when auto-closing if nothing else closed the sample.
     * @return the initialized sampler (auto-closeable)
     */
    public MetrigoSample getSamplerAndStart(MetrigoMetricAccumulator errorAccumulator) {
        MetrigoSampleSingleThreadImpl impl = new MetrigoSampleSingleThreadImpl();
        impl.setErrorStats(errorAccumulator);
        impl.start();
        return impl;
    }

    /**
     * Get a cross thread sampler.
     * 
     * This sampler is less accurate, but can be used across threads. This could be useful when queuing or 
     * sending out to a compute thread pool. It is the consumers responsibility to close this sampler, but
     * the standard try-with block will not work in the use cases where you would want this.
     * 
     * @param errorAccumulator the accumulator to use in the case of error
     * @return the initialized sampler.
     */
    public MetrigoSample getCrossThreadSamplerAndStart(MetrigoMetricAccumulator errorAccumulator) {
        MetrigoSampleCrossThreadImpl impl = new MetrigoSampleCrossThreadImpl();
        impl.setErrorStats(errorAccumulator);
        impl.start();
        return impl;
    }
}
