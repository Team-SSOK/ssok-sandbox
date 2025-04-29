package kr.ssok.kafka.messaging.server.service;

import kr.ssok.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankService {

    // 멱등성 보장을 위한 처리 이력 저장소 (실제로는 DB 사용)
    private final ConcurrentHashMap<String, TransferResponse> processedRequests = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${spring.kafka.request-topic}", groupId = "request-server-group")
    @SendTo // 응답은 헤더에 지정된 reply topic으로 전송됨
    public ResponseMessage processRequest(RequestMessage request,
                                          @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTo,
                                          @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {

        // 요청 처리 로직
        System.out.println("Server received request: " + request);

        // 비즈니스 로직 처리
        String processedData = processBusinessLogic(request.getData());

        // 응답 생성 및 반환
        ResponseMessage response = new ResponseMessage();
        response.setCorrelationId(request.getCorrelationId());
        response.setData(processedData);

        System.out.println("Server sending response: " + response);
        return response;
    }

    private String processBusinessLogic(String data) {
        // 실제 비즈니스 로직 처리
        return "Processed: " + data + " at " + System.currentTimeMillis();
    }



//    @KafkaListener(topics = "${openbanking.kafka.request-topic}")
//    @SendTo  // 반환값이 응답 토픽으로 전송됨
//    public TransferResponse handleTransferRequest(@Payload TransferRequest request,
//                                                  @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId,
//                                                  @Header(KafkaHeaders.REPLY_TOPIC) String replyTopic) {
//
//        log.info("전송 받음!!!");
//        log.info("Received transfer request in bank service: {}", request);
//        log.info("Correlation ID: {}", new String(correlationId));
//        log.info("Reply topic: {}", replyTopic);
//
//        // 멱등성 체크: 이미 처리된 요청인지 확인
//        if (processedRequests.containsKey(request.getRequestId())) {
//            log.info("Duplicate request detected, returning cached response: {}", request.getRequestId());
//            return processedRequests.get(request.getRequestId());
//        }
//
//        // 실제 은행 송금 처리 로직 구현 (여기서는 간단히 시뮬레이션)
//        TransferResponse response = processTransferInBank(request);
//
//        // 처리 결과 캐싱 (멱등성 보장)
//        processedRequests.put(request.getRequestId(), response);
//
//        log.info("Transfer processed, sending response: {}", response);
//        return response;
//    }

    // 실제 은행 시스템에서의 송금 처리 로직 (시뮬레이션)
    private TransferResponse processTransferInBank(TransferRequest request) {
        try {
            // 실제로는 여기서 은행 핵심 시스템과의 통합 로직이 들어갈 것임
            // 계좌 유효성 검증, 잔액 확인, 송금 처리 등

            // 송금 처리 시뮬레이션 (랜덤 성공/실패)
            boolean isSuccess = Math.random() > 0.2;  // 80% 확률로 성공

            if (isSuccess) {
                return TransferResponse.builder()
                        .requestId(request.getRequestId())
                        .transactionId(UUID.randomUUID().toString())
                        .status(TransferStatus.SUCCESS)
                        .message("Transfer completed successfully")
                        .processedTime(LocalDateTime.now())
                        .build();
            } else {
                return TransferResponse.builder()
                        .requestId(request.getRequestId())
                        .status(TransferStatus.FAILED)
                        .message("Insufficient funds or other banking error")
                        .processedTime(LocalDateTime.now())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error processing transfer in bank", e);
            return TransferResponse.builder()
                    .requestId(request.getRequestId())
                    .status(TransferStatus.FAILED)
                    .message("Bank system error: " + e.getMessage())
                    .processedTime(LocalDateTime.now())
                    .build();
        }
    }
}
