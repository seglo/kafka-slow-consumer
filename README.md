# kafka-slow-consumer

A simple project used to simulate a slow consumer by using [reactive-kafka](https://doc.akka.io/docs/akka-stream-kafka/current/home.html) 
to consume and [akka streams](https://doc.akka.io/docs/akka/current/stream/stages-overview.html?language=scala#throttle) to throttle
consumed traffic.  This project was created to help demonstrate monitoring consumer lag. 

## Creating a producer

Use `kafka-producer-perf-test` from Confluent Kafka docker to produce data.

i.e.)

```
# produce 100 msg/s off messages 100 bytes in size.  
$ sudo docker run -it confluentinc/cp-kafka kafka-producer-perf-test \
--topic slow-consumer-topic \
--num-records 1000000 \
--record-size 100 \
--throughput 100 \
--producer-props bootstrap.servers=broker.kafka.l4lb.thisdcos.directory:9092

501 records sent, 100.2 records/sec (0.01 MB/sec), 4.1 ms avg latency, 284.0 max latency.
502 records sent, 100.3 records/sec (0.01 MB/sec), 1.5 ms avg latency, 7.0 max latency.
500 records sent, 100.0 records/sec (0.01 MB/sec), 2.1 ms avg latency, 44.0 max latency.
501 records sent, 100.0 records/sec (0.01 MB/sec), 2.2 ms avg latency, 16.0 max latency.
501 records sent, 100.0 records/sec (0.01 MB/sec), 2.7 ms avg latency, 21.0 max latency.
501 records sent, 100.2 records/sec (0.01 MB/sec), 1.3 ms avg latency, 28.0 max latency.
...
```

## Exporting metrics

Add the following JVM flags to expose the JVM metrics.

```
-Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

To expose a prometheus endpoint using the Prometheus JMX exporter, add it as a java agent using these JVM flags.

```
-javaagent:./lib/jmx_prometheus_javaagent-0.3.1.jar=8080:./lib/jmx-prometheus-exporter-config.yaml
```  