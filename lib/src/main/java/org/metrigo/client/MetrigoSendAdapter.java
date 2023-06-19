package org.metrigo.client;

public interface MetrigoSendAdapter {
    public void sendMetrics(MetrigoMetricAccumulator stats);
}
