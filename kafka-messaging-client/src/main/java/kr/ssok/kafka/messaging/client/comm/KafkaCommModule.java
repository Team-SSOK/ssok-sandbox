package kr.ssok.kafka.messaging.client.comm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCommModule {
    private final ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate;

    @Value("${spring.kafka.request-topic}")
    private String requestTopic;

    @PostConstruct
    public void init() {
        replyingKafkaTemplate.start();
    }

    public CommQueryPromise sendPromiseQuery(String key, Object request, int timeout)
    {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(requestTopic, key , request);

        log.info("Sending Promise Request: {}", request);

        RequestReplyFuture<String, Object, Object> future =
                this.replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(timeout));

        return new CommQueryPromise(future);
    }

    public ReplyingKafkaTemplate<String, Object, Object> getReplyingKafkaTemplate() {
        return replyingKafkaTemplate;
    }

}
