# SSOK-SANDBOX
### Kafka-Messaging-Sample

SSOK 프로젝트에서 적용할 카프카 클라이언트/서버 예제입니다. 

* kafka-messaging-client 
* kafka-messaging-server

클라이언트에서 서버에 메세지를 보내기 위해서는 KafkaCommModule를 의존성 주입하여 사용합니다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenBankingService {

    private final KafkaCommModule commModule;
    ...       
}
```



## 클라이언트/서버에서 사용할 KEY 정의

클라이언트와 서버에서 메세지를 주고 받을때 사용하는 공용키를 정의합니다. 

``` java
public class CommunicationProtocol {

    public static final String REQUEST_DEPOSIT = "kr.ssok.kafka.messaging.request.deposit";
    public static final String REQUEST_WITHDRAW = "kr.ssok.kafka.messaging.request.withdraw";
    public static final String SEND_TEST_MESSAGE = "kr.ssok.kafka.messaging.test.message";
    
}
```



## 메세지 전송 요청 방식 (클라이언트에서 수행)

메세지 전송 방식은 2가지 방식을 제공합니다. (프로미스 메세지, 단방향 메세지)

#### 1. 프로미스

전통적인 요청-응답 패턴입니다, 요청 메세지 전송후 응답 메세지를 받습니다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaClientService {

    //KafkaCommModule 의존성 주입
    private final KafkaCommModule commModule;

    /**
     * KafkaCommModule의 sendPromiseQuery를 사용하는 방식
     * @param request
     * @return
     */
    public TransferResponse processTransfer(TransferRequest request) {
        try {

            // sendPromiseQuery 호출
            CommQueryPromise promise = this.commModule.sendPromiseQuery(CommunicationProtocol.REQUEST_DEPOSIT, request, 30);

            // CommQueryPromise의 Future로 PromiseMessage(응답 메세지)를 가져옵니다.
            // 요청에 대한 응답을 받기 전까지 해당 스레드를 대기합니다.
            PromiseMessage msg = promise.get();

            // PromiseMessage에서 응답받은 데이터를 가져옵니다.
            TransferResponse result = msg.getDataObject(TransferResponse.class);

            log.info("Received Promise response: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Error processing Promise", e);
            return TransferResponse.builder()
                    .status(TransferStatus.FAILED)
                    .build();
        }
    }
    
 	...   
        
}
```

#### 2. 단방향 메세지

클라이언트에서 서버에 단방향 메세지를 전송합니다.

```JAVA
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaClientService {

    //KafkaCommModule 의존성 주입
    private final KafkaCommModule commModule;
    
    /**
     * 단방향으로 메세지를 전송합니다.
     * @param message
     */
    public void sendUnidirectionalMessage(String message)
    {
        this.commModule.sendMessage(CommunicationProtocol.SEND_TEST_MESSAGE, (Object) message , (sendResult, throwable) -> {
            if (throwable != null) log.error("메시지 전송 실패: ", throwable);
			else log.info("메시지 전송 성공!");
        });
    }
    
    ...
    
}
```



## 메세지 전송 응답 방식 (서버에서 수행)

#### 1. 프로미스

전통적인 요청-응답 패턴입니다, 요청 받은 메시지를 확인하고 리스너에서 응답을 반환합니다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServerService {
    /**
     * 프로미스 요청에 대한 카프카 리스너
     * 요청한 내용을 확인후 응답을 반환합니다.
     * (kafkaListenerReplyContainerFactory 사용)
     *
     * @param request DTO 객체
     * @param key 식별자 키
     * @param replyTopic 응답해야하는 토픽
     * @param correlationId 상관 ID
     * @return
     */
    @KafkaListener(topics = "${spring.kafka.request-topic}", groupId = "request-server-group", containerFactory = "kafkaListenerReplyContainerFactory")
    @SendTo // 응답은 헤더에 지정된 replyTopic으로 전송됨
    public TransferResponse handleTransferRequest(TransferRequest request,
                                          @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                          @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTopic,
                                          @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {

        log.info("Correlation ID: {}", new String(correlationId));
        log.info("Reply topic: {}", replyTopic);
        log.info("Reply KEY: {}", key);

        // 요청 처리 로직
        log.info("Server received request: {}", request);

        // 실제 은행 송금 처리 로직 구현 (여기서는 간단히 시뮬레이션)
        TransferResponse response = TransferResponse.builder()
                    .requestId(request.getRequestId())
                    .transactionId(UUID.randomUUID().toString())
                    .status(TransferStatus.SUCCESS)
                    .message("Transfer completed successfully")
                    .processedTime(LocalDateTime.now())
                    .build();
        
        switch (key) {
            case CommunicationProtocol.SEND_TEST_MESSAGE:
                log.info("Called SEND_TEST_MESSAGE!");
                break;
            case CommunicationProtocol.REQUEST_DEPOSIT:
                log.info("Called REQUEST_DEPOSIT!");
                break;
            case CommunicationProtocol.REQUEST_WITHDRAW:
                log.info("Called REQUEST_WITHDRAW!");
                break;
        }
        
        log.info("Transfer processed, sending response: {}", response);
        return response;
    }
```

#### 2. 단방향 메세지

클라이언트에서 전달받은 메세지를 확인합니다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServerService {
     /**
     * 단방향 메세지 요청에 대한 카프카 리스너
     * (kafkaListenerUnidirectionalContainerFactory 사용)
     *
     * @param key 식별자 키
     * @param value DTO 객체
     */
    @KafkaListener(topics = "${spring.kafka.push-topic}", containerFactory = "kafkaListenerUnidirectionalContainerFactory")
    public void receiveMessage(@Header(KafkaHeaders.RECEIVED_KEY) String key, Object value) {
        log.info("Received unidirectional message in bank service: {}", value);
        log.info("Received KEY: {}", key);
        
        switch (key) {
            // 여기서는 간단히 로그 출력
            case CommunicationProtocol.SEND_TEST_MESSAGE:
                log.info("Hello World!");
                break;
            case CommunicationProtocol.REQUEST_DEPOSIT:
                log.info("Hello World!!");
                break;
            case CommunicationProtocol.REQUEST_WITHDRAW:
                log.info("Hello World!!!");
                break;
        }
    }
    
}
```

