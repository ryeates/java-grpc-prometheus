// Copyright 2016 Dino Wernli. All Rights Reserved. See LICENSE for licensing terms.

package me.dinowernli.grpc.prometheus;

import java.io.IOException;
import java.time.Clock;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import io.prometheus.client.exporter.HTTPServer;

/** A {@link ClientInterceptor} which sends stats about incoming grpc calls to Prometheus. */
public class MonitoringClientInterceptor implements ClientInterceptor {
  private final Clock clock;
  private final Configuration configuration;
  private final ClientMetrics.Factory clientMetricsFactory;

  public static MonitoringClientInterceptor create(Configuration configuration) {
    return new MonitoringClientInterceptor(
        Clock.systemDefaultZone(), configuration, new ClientMetrics.Factory(configuration));
  }

  private MonitoringClientInterceptor(
      Clock clock,
      Configuration configuration,
      ClientMetrics.Factory clientMetricsFactory) {
    this.clock = clock;
    this.configuration = configuration;
    this.clientMetricsFactory = clientMetricsFactory;
	try {
	  if (configuration.isProvideExpositionServer()) {
	    new HTTPServer(configuration.getPort());
      }
	} catch (IOException e) {
      throw new RuntimeException("Failed to start exposition HTTP server on port " +configuration.getPort());
	}
  }

  @Override
  public <R, S> ClientCall<R, S> interceptCall(
      MethodDescriptor<R, S> methodDescriptor, CallOptions callOptions, Channel channel) {
    ClientMetrics metrics = clientMetricsFactory.createMetricsForMethod(methodDescriptor);
    return new MonitoringClientCall<>(
        channel.newCall(methodDescriptor, callOptions),
        metrics,
        GrpcMethod.of(methodDescriptor),
        configuration,
        clock);
  }
}
