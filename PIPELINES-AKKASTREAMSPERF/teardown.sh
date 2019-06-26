#!/usr/bin/env bash
kubectl delete deployment kafka-slow-consumer -n seglo
kubectl delete deployment producers -n seglo
kubectl delete kafkatopic simple-topic -n lightbend