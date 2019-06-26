#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
kubectl apply -f $DIR/simple-topic.yaml -n lightbend
kubectl apply -f $DIR/kafka-slow-consumer.yaml -n seglo
kubectl apply -f $DIR/producers.yaml -n seglo
