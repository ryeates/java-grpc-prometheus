# java-grpc-prometheus

Java interceptors which can be used to monitor Grpc services using Prometheus.

Forked as the grpc-ecosystem version looks dead and gRPC has moved on.

## Features

The features of this library include two monitoring grpc interceptors, `MonitoringServerInterceptor` and `MonitoringClientInterceptor`. These interceptors can be attached separately to grpc servers and client stubs respectively. For each RPC, the interceptors increment the following Prometheus metrics, broken down by method type, service name, method name, and response code:

* Server
    * `grpc_server_started_total`: Total number of RPCs started on the server.
    * `grpc_server_handled_total`: Total number of RPCs completed on the server, regardless of success or failure.
    * `grpc_server_handled_latency_seconds`: (Optional) Histogram of response latency of rpcs handled by the server, in seconds.
    * `grpc_server_msg_received_total`: Total number of stream messages received from the client.
    * `grpc_server_msg_sent_total`: Total number of stream messages sent by the server.
* Client
    * `grpc_client_started_total`: Total number of RPCs started on the client.
    * `grpc_client_completed`: Total number of RPCs completed on the client, regardless of success or failure.
    * `grpc_client_completed_latency_seconds`: (Optional) Histogram of rpc response latency for completed rpcs, in seconds.
    * `grpc_client_msg_received_total`: Total number of stream messages received from the server.
    * `grpc_client_msg_sent_total`: Total number of stream messages sent by the client.
    
Note that by passing a `Configuration` instance to the interceptors, it is possible to configure the following:
* Whether or not a latency histogram is recroded for rpcs.
* Which historam buckets to use for the latency metrics.
* Which Prometheus `CollectorRegistry` the metrics get registered with.

The server interceptors have an identical implementation in Golang, [go-grpc-prometheus](https://github.com/mwitkow/go-grpc-prometheus), brought to you by [@MWitkow](http://twitter.com/mwitkow).

## Usage

In order to attach the monitoring server interceptor to your gRPC server, you can do the following:

```java
MonitoringServerInterceptor monitoringInterceptor = 
    MonitoringServerInterceptor.create(Configuration.cheapMetricsOnly());
grpcServer = ServerBuilder.forPort(GRPC_PORT)
    .addService(ServerInterceptors.intercept(
        HelloServiceGrpc.bindService(new HelloServiceImpl()), monitoringInterceptor))
    .build();
```

In order to attach the monitoring client interceptor to your gRPC client, you can do the following:

```java
MonitoringClientInterceptor monitoringInterceptor =
    MonitoringClientInterceptor.create(Configuration.cheapMetricsOnly());
grpcStub = HelloServiceGrpc.newStub(NettyChannelBuilder.forAddress(REMOTE_HOST, GRPC_PORT)
    .intercept(monitoringInterceptor)
    .build());
```

By default there is no exposition server provided for Prometheus to scrape. Using the `withPort(<somePortNumber>)` starts the basic Prometheus provided HTTPServer on the given port.
## Related reading

* [gRPC](http://grpc.io)
* [Prometheus](http://prometheus.io)

## Updates

* Move to gRPC 1.13.1
* Move to Java 11
* Clean up test shutdown

## TODO

* Move to Gradle KT and offer patch back to dead? ecosystem repo
* Maven Central
