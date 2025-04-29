package kr.ssok.kafka.messaging.client.service;

import jakarta.annotation.PostConstruct;
import kr.ssok.model.RequestMessage;
import kr.ssok.model.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate;

    @Value("${spring.kafka.request-topic}")
    private String requestTopic;

    @PostConstruct
    public void init() {
        replyingKafkaTemplate.start();
    }

    public ResponseMessage sendAndReceive(String data) throws Exception {
        // 요청 메시지 생성
        RequestMessage request = new RequestMessage();
        request.setCorrelationId(UUID.randomUUID().toString());
        request.setData(data);

        // 메시지 레코드 생성
        ProducerRecord<String, Object> record = new ProducerRecord<>(requestTopic, request);

        // 요청 발송 및 응답 대기
        RequestReplyFuture<String, Object, Object> future =
                replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(10));

        System.out.println("Client sent request: " + request);

        // 응답 수신 및 변환
        ConsumerRecord<String, Object> response = future.get();
        System.out.println("Client received response: " + response.value());

        return (ResponseMessage) response.value();
    }
}
