kubectl apply -f simple-topic-KAFKA-221.yaml -n lightbend
kubectl apply -f kafka-slow-consumer-KAFKA-221.yaml -n seglo
kubectl apply -f producer-KAFKA-221.yaml -n seglo
kubectl apply -f simple-topic-KAFKA-240-SNAPSHOT.yaml -n lightbend
kubectl apply -f kafka-slow-consumer-KAFKA-240-SNAPSHOT.yaml -n seglo
kubectl apply -f producer-KAFKA-240-SNAPSHOT.yaml -n seglo
