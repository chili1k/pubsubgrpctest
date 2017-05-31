package org.acme;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.pubsub.spi.v1.Publisher;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Duration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App
{

    private static final long TOTAL_TIMEOUT = 11000L;

    private static void publish(Publisher publisher, ByteString payload)  {
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(payload).build();

        final Stopwatch timer = Stopwatch.createStarted();
        ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);

        ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {
            public void onFailure(Throwable throwable) {
                timer.stop();
                log.warn("Publish failed after {} ms. {}", timer.elapsed(TimeUnit.MILLISECONDS), throwable.getMessage());
            }

            public void onSuccess(String messageId) {
                timer.stop();
                long elapsed = timer.elapsed(TimeUnit.MILLISECONDS);

                if (elapsed > TOTAL_TIMEOUT) {
                    log.warn("Publish succeeded. Timeout period breached with duration of {} ms", elapsed);
                } else {
                    log.info("Published in {} ms! [{}]", elapsed, messageId);
                }
            }
        });
    }

    public static void main(String[] args) throws Exception
    {
        String project = "tcoffee-test";
        String topic = "apiTest";

        TopicName topicName = TopicName.create(project, topic);

        RetrySettings retrySettings = RetrySettings.newBuilder()
                .setInitialRetryDelay(Duration.ofMillis(5))
                .setInitialRpcTimeout(Duration.ofMillis(200))
                .setRpcTimeoutMultiplier(2.0)
                .setRetryDelayMultiplier(2.0)
                .setMaxRetryDelay(Duration.ofMillis(4000))
                .setMaxRpcTimeout(Duration.ofMillis(4000))
                .setTotalTimeout(Duration.ofMillis(TOTAL_TIMEOUT))
                .build();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("tcoffee-test-a021a69bda91.json");

        Publisher publisher = Publisher.defaultBuilder(topicName)
            // .setCredentials(ServiceAccountCredentials.fromStream(input))
                .setRetrySettings(retrySettings)
                .build();

        ByteString payload = ByteString.copyFrom("Test payload", Charset.forName(Charsets.UTF_8.name()));


        while (true) {
            log.info("Publishing message");
            publish(publisher, payload);
            Thread.sleep(3000);
        }
    }
}
