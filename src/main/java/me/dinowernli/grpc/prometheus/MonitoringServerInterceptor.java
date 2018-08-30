// Copyright 2016 Dino Wernli. All Rights Reserved. See LICENSE for licensing terms.

package me.dinowernli.grpc.prometheus;

import java.io.IOException;
import java.time.Clock;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.prometheus.client.exporter.HTTPServer;

/** A {@link ServerInterceptor} which sends stats about incoming grpc calls to Prometheus. */
public class MonitoringServerInterceptor implements ServerInterceptor {
  private final Clock clock;
  private final Configuration configuration;
  private final ServerMetrics.Factory serverMetricsFactory;

  public static MonitoringServerInterceptor create(Configuration configuration) {
    return new MonitoringServerInterceptor(
        Clock.systemDefaultZone(), configuration, new ServerMetrics.Factory(configuration));
  }

  private MonitoringServerInterceptor(
      Clock clock, Configuration configuration, ServerMetrics.Factory serverMetricsFactory) {
    this.clock = clock;
    this.configuration = configuration;
    this.serverMetricsFactory = serverMetricsFactory;
	try {
      if (configuration.isProvideExpositionServer()) {
		  new HTTPServer(configuration.getPort());
      }
	} catch (IOException e) {
	  throw new RuntimeException("Failed to start exposition HTTP server on port " +configuration.getPort());
	}
  }

  @Override
  public <R, S> ServerCall.Listener<R> interceptCall(
      ServerCall<R, S> call,
      Metadata requestHeaders,
      ServerCallHandler<R, S> next) {
    MethodDescriptor<R, S> method = call.getMethodDescriptor();
    ServerMetrics metrics = serverMetricsFactory.createMetricsForMethod(method);
    GrpcMethod grpcMethod = GrpcMethod.of(method);
    ServerCall<R,S> monitoringCall = new MonitoringServerCall(call, clock, grpcMethod, metrics, configuration);
    return new MonitoringServerCallListener<>(
        next.startCall(monitoringCall, requestHeaders), metrics, GrpcMethod.of(method));
  }

}
