# About
Pub/Sub grpc client example which publishes a message to Pub/Sub every three seconds and prints out results.

# Usage
Build and run:

```
mvn clean install
# Replace myProject and myPubSubTopic with your own
java -jar ./target/pubsubgrpctest.jar myProject myPubSubTopic
```

# Issues
If there is packet loss or latency introduced in the network, the client doesn't time out after 11 seconds which is the configured total timeout.

To simulate network latency use lartc (client should run a couple of minutes):

```
# Add latency
tc qdisc add dev eth0 root netem loss 80%

# Run client (replace myProject and myPubSubTopic with your own)
java -jar ./target/pubsubgrpctest.jar myProject myPubSubTopic
```

After a few minutes there should be some very long latencies:

```
03:52:18.671 [pool-2-thread-6] WARN  org.acme.App - Publish failed after 40272 ms. DEADLINE_EXCEEDED
03:52:18.671 [pool-2-thread-6] WARN  org.acme.App - Publish failed after 31248 ms. DEADLINE_EXCEEDED
03:52:18.672 [pool-2-thread-6] WARN  org.acme.App - Publish failed after 34253 ms. DEADLINE_EXCEEDED
03:52:18.672 [pool-2-thread-6] WARN  org.acme.App - Publish failed after 37256 ms. DEADLINE_EXCEEDED
```

Remove latency:

```
tc qdisc del dev eth0 root netem
```
