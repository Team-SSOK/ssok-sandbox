package kr.ssok.kafka.messaging.server.config;

import kr.ssok.model.TransferRequest;
import kr.ssok.model.TransferResponse;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

//서버 config
@Configuration
public class KafkaConfig {

    @Value("${openbanking.kafka.request-topic}")
    private String requestTopic;

    @Value("${openbanking.kafka.reply-topic}")
    private String replyTopic;

    @Value("${openbanking.kafka.timeout}")
    private long replyTimeout;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Autowired
    private ProducerFactory<String, TransferResponse> producerFactory;

    @Bean
    public NewTopic requestTopic() {
        return TopicBuilder.name(requestTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic replyTopic() {
        return TopicBuilder.name(replyTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // 요청 메시지용 Consumer Factory
    @Bean
    public ConsumerFactory<String, TransferRequest> requestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bank-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "kr.ssok.model");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 응답 메시지용 Consumer Factory
    @Bean
    public ConsumerFactory<String, TransferResponse> replyConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "openbanking-replies");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "kr.ssok.model");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 요청 처리용 리스너 컨테이너 팩토리
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransferRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransferRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(requestConsumerFactory());
        factory.setReplyTemplate(replyTemplate());
        return factory;
    }

    // 응답 수신용 리스너 컨테이너 팩토리
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransferResponse> replyListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransferResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(replyConsumerFactory());
        return factory;
    }

    // 응답을 받기 위한 리스너 컨테이너 설정
    @Bean
    public ConcurrentMessageListenerContainer<String, TransferResponse> repliesContainer() {
        ConcurrentMessageListenerContainer<String, TransferResponse> repliesContainer =
                replyListenerContainerFactory().createContainer(replyTopic);
        repliesContainer.getContainerProperties().setGroupId("openbanking-replies");
        repliesContainer.setAutoStartup(true);
        return repliesContainer;
    }

    // 응답 전송을 위한 KafkaTemplate
    @Bean
    public KafkaTemplate<String, TransferResponse> replyTemplate() {
        return new KafkaTemplate<>(producerFactory);
    }

    // ReplyingKafkaTemplate 설정
    @Bean
    public ReplyingKafkaTemplate<String, TransferRequest, TransferResponse> replyingKafkaTemplate(
            ProducerFactory<String, TransferRequest> pf) {
        ReplyingKafkaTemplate<String, TransferRequest, TransferResponse> template =
                new ReplyingKafkaTemplate<>(pf, repliesContainer());
        template.setDefaultReplyTimeout(Duration.ofMillis(replyTimeout));
        return template;
    }
}
