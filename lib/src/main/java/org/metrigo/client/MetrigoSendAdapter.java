package org.metrigo.client;

import java.util.Date;

import org.metrigo.MetrigoStatistics;

public interface MetrigoSendAdapter {
    /**
     * Send statistics to the back-end.
     * 
     * Of note with this interface. All error handling should be done internally in this routine, and
     * there should be great effort made to ensure that this routine does not block. In most cases, we
     * would want to put a queue between this call and the actual sending of information over a wire.
     * The reason to leave this to the implementation is that many implementations already have such
     * queuing built in. We don't want to enforce our own queueing on top.
     * 
     * @param name the name of the statistic
     * @param time the time of collection
     * @param stats the actual statistics to send.
     */
    public void sendMetrics(final String name, final Date time, final MetrigoStatistics stats);
}
