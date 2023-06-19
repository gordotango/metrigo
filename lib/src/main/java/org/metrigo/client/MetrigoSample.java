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

 /**
  * A sampler for Metrigo.
  *
  * This sampler allows for generating a time sample to add to a set of
  * stats.
  *
  * Usage should be of the form
  * <pre>
  * try (MetrigoSample sample = MetrigoApi.startSample(errorStats) {
  *     ...
  *     sample.finish(goodStats);
  * }
  * </pre>
  */
 public interface MetrigoSample extends AutoCloseable {
    /**
     * Start the sample period.
     * 
     * This method should be called at the beginning of the code that is to be observed.
     */
    void start();

    /**
     * End the sample gathering, and publish to a statistics group.
     * 
     * This is the normal path to completion. It will properly register the
     * new sample into the stats accumulating it so that it will eventually be
     * reported.
     * 
     * @param stats the statistics group for publishing.
     */
    void finish(MetrigoMetricAccumulator stats);

    /**
     * Clean up the sampling.
     * 
     * This method must always be called (try with will handle that). If the
     * sample has not been registered with a statistics group, the implementation
     * should provide for a method to specify an error statistics group.
     */
    void close();
 }
