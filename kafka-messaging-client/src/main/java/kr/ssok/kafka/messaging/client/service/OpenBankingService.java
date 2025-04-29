package kr.ssok.kafka.messaging.client.service;


import kr.ssok.model.TransferRequest;
import kr.ssok.model.TransferResponse;
import kr.ssok.model.TransferStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenBankingService {

//    private final ReplyingKafkaTemplate<String, TransferRequest, TransferResponse> kafkaTemplate;
//
//    @Value("${openbanking.kafka.request-topic}")
//    private String requestTopic;
//
//    @Value("${openbanking.kafka.timeout:30000}")
//    private long replyTimeout;
//
//    @Value("${openbanking.kafka.reply-topic}")
//    private String replyTopic;
//
//    @Transactional("kafkaTransactionManager")
//    public TransferResponse processTransfer(TransferRequest request) {
//        try {
//
//            // 요청 ID 생성 (없는 경우)
//            if (request.getRequestId() == null) {
//                request.setRequestId(UUID.randomUUID().toString());
//            }
//
//            // 요청 시간 설정
//            request.setRequestTime(LocalDateTime.now());
//
//            String correlationId = request.getRequestId();
//
//            // Kafka를 통해 은행에 송금 요청 전송
//            ProducerRecord<String, TransferRequest> record =
//                    new ProducerRecord<>(requestTopic, request.getRequestId(), request);
//
//            log.info("Sending transfer request: {}", request);
//
//            // RequestReplyFuture를 사용하여 응답 대기
//            RequestReplyFuture<String, TransferRequest, TransferResponse> future =
//                    kafkaTemplate.sendAndReceive(record);
//
//            // 응답 대기 및 처리
//            ConsumerRecord<String, TransferResponse> response = future.get(30, TimeUnit.SECONDS);
//            TransferResponse result = response.value();
//
//            log.info("Received transfer response: {}", result);
//            return result;
//
//        } catch (Exception e) {
//            log.error("Error processing transfer request", e);
//            return TransferResponse.builder()
//                    .requestId(request.getRequestId())
//                    .status(TransferStatus.FAILED)
//                    .message("Failed to process transfer: " + e.getMessage())
//                    .processedTime(LocalDateTime.now())
//                    .build();
//        }
//    }
}

